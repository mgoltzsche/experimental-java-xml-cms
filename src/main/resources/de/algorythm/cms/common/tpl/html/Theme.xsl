<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:c="http://cms.algorythm.de/common/CMS"
	xmlns:p="http://cms.algorythm.de/common/Pages"
	xmlns="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="c">

	<xsl:template match="/c:page | /c:site">
		<xsl:param name="repositoryDirectory" />
		<html>
			<head>
				<title>
					<xsl:value-of select="current()/@title" />
				</title>
			</head>
			<body>
				<nav>
					<ul>
						<xsl:apply-templates select="document(concat($repositoryDirectory, '/example1.org/pages.xml'))/p:page/*"/>
					</ul>
				</nav>
				<xsl:if test="current()/@subNav='true'">
					<nav id="secondary-nav"></nav>
				</xsl:if>
				<main>
					<h2>
						<xsl:value-of select="current()/@title" />
					</h2>
					<xsl:apply-templates />
				</main>
			</body>
			</html>
	</xsl:template>
</xsl:stylesheet>
