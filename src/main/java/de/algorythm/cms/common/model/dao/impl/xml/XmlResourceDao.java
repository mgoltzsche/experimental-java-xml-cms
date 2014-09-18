package de.algorythm.cms.common.model.dao.impl.xml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.algorythm.cms.common.Configuration;
import de.algorythm.cms.common.LocaleResolver;
import de.algorythm.cms.common.impl.xml.InformationCompleteException;
import de.algorythm.cms.common.impl.xml.Constants;
import de.algorythm.cms.common.impl.xml.contentHandler.IncludingHandler;
import de.algorythm.cms.common.impl.xml.contentHandler.PageInfoHandler;
import de.algorythm.cms.common.model.entity.IPage;
import de.algorythm.cms.common.model.entity.ISite;
import de.algorythm.cms.common.model.entity.impl.Site;
import de.algorythm.cms.common.renderer.impl.xml.IXmlReaderFactory;
import de.algorythm.cms.common.util.FilePathUtil;

public class XmlResourceDao {

	static private final Logger log = LoggerFactory.getLogger(XmlResourceDao.class);
	
	private final Locale defaultLocale;
	private final File repositoryDirectory;
	private final XMLInputFactory staxReaderFactory = XMLInputFactory.newInstance();
	private final IXmlReaderFactory saxReaderFactory;
	private final LocaleResolver locales;
	
	public XmlResourceDao(final Configuration cfg, final LocaleResolver locales, final IXmlReaderFactory readerFactory) {
		repositoryDirectory = cfg.repository;
		defaultLocale = cfg.defaultLanguage;
		this.locales = locales;
		this.saxReaderFactory = readerFactory;
	}
	
	public List<ISite> getSites() {
		final List<ISite> sites = new LinkedList<ISite>();
		final File[] rootFiles = repositoryDirectory.listFiles();
		
		for (File siteDir : rootFiles) {
			if (siteDir.isDirectory()) {
				final String siteName = siteDir.getName();
				final File siteXmlFile = new File(siteDir, "site.xml");
				final String title;
				final Locale defaultLocale;
				final String contextPath;
				
				if (siteXmlFile.isFile()) {
					try {
						final Map<String, String> attr = readRootTag(siteXmlFile, Constants.Namespace.SITE, Constants.Tag.SITE);
						final String localeStr = attr.get("default-language");
						defaultLocale = localeStr == null ? this.defaultLocale : locales.getLocale(localeStr);
						title = attr.get("title");
						contextPath = attr.get("context-path");
					} catch (Exception e) {
						log.error("Cannot read " + siteXmlFile, e);
						continue;
					}
				} else {
					title = siteName;
					contextPath = "";
					defaultLocale = this.defaultLocale;
				}
				
				sites.add(new Site(this, siteName, title, defaultLocale, contextPath));
			}
		}
		
		return sites;
	}
	
	public List<IPage> loadPages(String site, String path) throws SAXException {
		final String relativeDir = FilePathUtil.toSystemSpecificPath(site + "/pages" + path);
		final File directory = new File(repositoryDirectory, relativeDir);
		
		if (!directory.exists())
			throw new IllegalStateException(relativeDir + " does not exist");
		
		if (!directory.isDirectory())
			throw new IllegalStateException(relativeDir + " is not a directory");
		
		final LinkedList<IPage> pages = new LinkedList<IPage>();
		final PageInfoHandler pageInfo = new PageInfoHandler();
		final IncludingHandler handler = new IncludingHandler(saxReaderFactory, pageInfo);
		final XMLReader reader = saxReaderFactory.createReader();
		
		reader.setContentHandler(handler);
		reader.setErrorHandler(handler);
		
		for (File subDirectory : directory.listFiles()) {
			if (subDirectory.isDirectory()) {
				final File xmlFile = new File(subDirectory, "page.xml");
				
				if (xmlFile.exists() && xmlFile.isFile()) {
					try {
						reader.parse(xmlFile.getAbsolutePath());
					} catch(InformationCompleteException e) {
					} catch(Exception e) {
						log.error("Cannot read " + xmlFile, e);
						continue;
					}
					
					pages.add(pageInfo.createPageInfo(this, site, path, subDirectory.getName()));
				}
			}
		}
		
		return pages;
	}
	
	private Map<String, String> readRootTag(final File xmlFile, final String expectedNamespace, final String expectedTag) throws XMLStreamException, IOException {
		try (final FileReader xmlFileReader = new FileReader(xmlFile)) {
			final XMLStreamReader reader = staxReaderFactory.createXMLStreamReader(xmlFileReader);
			
			try {
				while (reader.next() != XMLStreamReader.START_ELEMENT) {}
				
				final String namespace = reader.getNamespaceURI();
				final String localName = reader.getLocalName();
				final Map<String, String> attr = new HashMap<String, String>();
				
				if (!expectedNamespace.equals(namespace))
					throw new XMLStreamException("Unexpected namespace '" + namespace + "'. Expected '" + Constants.Namespace.SITE + '\'');
				
				if (!expectedTag.equals(localName))
					throw new XMLStreamException("Unexpected root tag '" + localName + "'. Expected '" + Constants.Tag.SITE + '\'');
				
				for (int i = 0; i < reader.getAttributeCount(); i++) {
					final String attrName = reader.getAttributeLocalName(i);
					final String attrValue = reader.getAttributeValue(i);
					
					attr.put(attrName, attrValue);
				}
				
				return attr;
			} finally {
				reader.close();
			}
		}
	}
}
