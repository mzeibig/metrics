<?xml version="1.0" encoding='ISO-8859-1' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns="http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul">
<xsl:output method="xml" indent="yes" encoding='ISO-8859-1'/>


<xsl:variable name="summecl">
   <xsl:value-of select="count(deepjava/deep/class)"/>
</xsl:variable>

<xsl:template name="TOOLTIP">
    <xsl:param name="n">0</xsl:param>
    <tooltip id="mytip0" orient="vertical" maxheight="500">
        <xsl:attribute name="id">mytip<xsl:value-of select="$n"/></xsl:attribute>
        <xsl:for-each select="/deepjava/deep/class[. = $n]">
            <description><xsl:value-of select="./@name"/></description>
        </xsl:for-each>
    </tooltip>
</xsl:template>

<xsl:template match="deepjava">
    <xsl:processing-instruction name="xml-stylesheet">href="deep.css" type="text/css"</xsl:processing-instruction>
    <window xmlns:html="http://www.w3.org/1999/xhtml"
            xmlns="http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul"
            class="main">

        <xsl:for-each select="./stats/sum[. &gt; 0]">
            <xsl:call-template name="TOOLTIP">
                <xsl:with-param name="n"><xsl:value-of select="./@order"/></xsl:with-param>
            </xsl:call-template>
        </xsl:for-each>
        <box>
            <vbox flex="1">
                    <description class="head">deepjava Analysis - Verteilung der Vererbungstiefe der <xsl:value-of select="$summecl"/> Klassen</description>
            </vbox>
            <vbox  align="end">
                <hbox>
                        <description class="info">Generation time: <xsl:value-of select="./date"/> / <xsl:value-of select="./time"/></description>
                </hbox>
                <hbox>
                        <description class="info">Designed for use with deepjava and Ant.</description>
                </hbox>
                <hbox>
                    <description class="info">View with Mozilla.</description>
                </hbox>

            </vbox>
        </box>
        <description class="line"></description>
        <hbox style="overflow:auto;" flex="1">
        <hbox> <description value=""/> </hbox>
            <grid>
                <columns>
                    <column width="110"/>
                    <column width="110"/>
                    <column width="70"/>
                    <column width="500"/>
                </columns>

                <rows>
                    <row style="background:#a6caf0;font-weight: bold;">
                      <hbox align="right" class="tabhead"><description value="Vererbungstiefe"/></hbox>
                      <hbox align="right" class="tabhead"><description value="Anzahl Klassen"/></hbox>
                      <hbox align="right" class="tabhead"><description value="Prozent"/></hbox>
                      <hbox align="right" class="tabhead"></hbox>
                    </row>

                    <xsl:for-each select="./stats/sum[. &gt; 0]">
                        <row style="background:#eeeee0;">
                            <hbox align="right" class="tabline">
                                <description><xsl:value-of select="./@order"/></description>
                            </hbox>
                            <hbox align="right" class="tabline">
                                <description align="right"><xsl:value-of select="."/></description>
                            </hbox>
                            <hbox align="right" class="tabline">
                                <description align="right">
                                    <xsl:value-of select="format-number(. div $summecl,'0.00%')"/>
                                </description>
                            </hbox>
                            <hbox  class="tabline">
                                <progressmeter mode="determined" tooltip="mytip"  flex="1">
                                    <xsl:attribute name="tooltip">mytip<xsl:value-of select="./@order"/></xsl:attribute>
                                    <xsl:attribute name="value"><xsl:value-of select="format-number(. div $summecl,'0.00%')"/></xsl:attribute>
                                </progressmeter>
                            </hbox>
                        </row>
                    </xsl:for-each>
                </rows>
            </grid>
        </hbox>
    </window>

</xsl:template>

</xsl:stylesheet>
