package de.algorythm.cms.common.impl.xml.contentHandler;

import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import de.algorythm.cms.common.LocaleResolver;
import de.algorythm.cms.common.impl.xml.Constants.Tag;
import de.algorythm.cms.common.model.entity.impl.SiteInfo;

public class SiteInfoHandler extends PageInfoHandler {

	private final LocaleResolver localeResolver;
	private final Locale defaultLocale;
	private SiteInfo siteInfo;
	
	public SiteInfoHandler(final LocaleResolver localeResolver, final Locale defaultLocale) {
		super(Tag.SITE);
		
		this.localeResolver = localeResolver;
		this.defaultLocale = defaultLocale;
	}
	
	public void setSite(final SiteInfo siteInfo) {
		this.siteInfo = siteInfo;
	}
	
	@Override
	protected void startRootElement(final String uri,
			final String localName, final Attributes atts) throws SAXException {
		final String name = siteInfo.getName();
		final String title = atts.getValue("site-title");
		final String descr = atts.getValue("description");
		final String lang = atts.getValue("default-language");
		final String tpl = atts.getValue("default-template");
		final String ctxPath = atts.getValue("context-path");
		
		siteInfo.setTitle(title != null && !title.trim().isEmpty() ? title : name);
		siteInfo.setDescription(descr);
		siteInfo.setDefaultLocale(lang == null ? defaultLocale : localeResolver.getLocale(lang));
		siteInfo.setDefaultTemplate(tpl);
		siteInfo.setContextPath(ctxPath == null || ctxPath.trim().isEmpty() ? "/" : ctxPath);
		
		super.startRootElement(uri, localName, atts);
	}
}