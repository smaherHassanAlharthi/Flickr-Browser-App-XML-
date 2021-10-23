package com.example.flickrbrowserappxml

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream


class XMLParser {
    private val ns: String? = null
    private val feeds = mutableListOf<Photo>()

    fun parse(inputStream: InputStream): List<Photo> {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return flickrData(parser)
        }
    }

    private fun flickrData(parser: XmlPullParser): List<Photo> {

        parser.require(XmlPullParser.START_TAG, ns, "rsp")

        while (parser.next() != XmlPullParser.END_TAG) {

            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            if (parser.name == "photos") {
                parser.require(XmlPullParser.START_TAG, ns, "photos")
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType != XmlPullParser.START_TAG) {
                        continue
                    }

                    if (parser.name == "photo")
                    {

                        parser.require(XmlPullParser.START_TAG, ns, "photo")
                        var id = parser.getAttributeValue(null, "id")
                        var owner = parser.getAttributeValue(null, "owner")
                        var secret = parser.getAttributeValue(null, "secret")
                        var server = parser.getAttributeValue(null, "server")
                        var farm = parser.getAttributeValue(null, "farm")
                        var title = parser.getAttributeValue(null, "title")
                        feeds.add(Photo(id, owner, secret, server, farm, title))
                        parser.nextTag()
                    }


                }

                    } else {
                        skip(parser)
                    }
        }
        return feeds

    }
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
