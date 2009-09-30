<?xml version="1.0" encoding='ISO-8859-1' ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns="http://www.w3.org/1999/xhtml">
<xsl:output method="xml" indent="yes" encoding='ISO-8859-1'/>


<xsl:variable name="summecl">
   <xsl:value-of select="count(deepjava/deep/class)"/>
</xsl:variable>

<xsl:template name="TOOLTIP">
    <xsl:param name="n">0</xsl:param>
    <xsl:attribute name="title">
        <xsl:for-each select="/deepjava/deep/class[. = $n]">
            <xsl:value-of select="./@name"/>,
        </xsl:for-each>
    </xsl:attribute>
</xsl:template>

<xsl:template match="deepjava">
    <html>
        <head>
            <style type="text/css">
                    @import url('deep.css');
            </style>
            
        </head>
        <body>
            <table width="100%">
                <tr>
                    <td align="left" style="margin: 0px 0px 5px; font: 130% verdana,arial,helvetica">deepjava Analysis - Verteilung der Vererbungstiefe der <xsl:value-of select="$summecl"/> Klassen</td>
                    <td align="right" style="font-size: 80%;">Generation time: <xsl:value-of select="./date"/> / <xsl:value-of select="./time"/><br/>
                    Designed for use with deepjava and Ant.</td>
                </tr>
            </table>
        <hr/>
        <table>
            <colgroup>
               <col width="110" />
               <col width="110" />
               <col width="70" />
                <col width="500" />
             </colgroup>
            <tr style="background:#a6caf0;font-weight: bold;">
                <th>Vererbungstiefe</th>
                <th>Anzahl Klassen</th>
                <th>Prozent</th>
                <th></th>
            </tr>
            <xsl:for-each select="./stats/sum[. &gt; 0]">
                <tr style="background:#eeeee0;">
                    <td align="right"><xsl:value-of select="./@order"/></td>
                    <td align="right"><xsl:value-of select="."/></td>
                    <td align="right"><xsl:value-of select="format-number(. div $summecl,'0.00%')"/>
                    </td>

                    <td>
                        <xsl:call-template name="TOOLTIP">
                            <xsl:with-param name="n"><xsl:value-of select="./@order"/></xsl:with-param>
                        </xsl:call-template>

                        <div>
                            <div style="width: 500px; font-size: 0px;" />
                            <div style="min-width: 40px;BORDER: #9c9c9c 1px solid;HEIGHT: 12px;WIDTH: 100%;">
                                <xsl:element name="div">
                                    <xsl:attribute name="style">width: <xsl:value-of select="format-number(. div $summecl,'0.0%')"/>;BACKGROUND: #00DF00;HEIGHT: 12px;</xsl:attribute>
                                </xsl:element>
                            </div>
                        </div>
                    </td>
                </tr>
            </xsl:for-each>
        </table>
        </body>
    </html>
</xsl:template>

</xsl:stylesheet>
