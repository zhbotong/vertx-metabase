package cc.shallow.vertx_metabase

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.spi.resolver.ResolverProvider.DISABLE_DNS_RESOLVER_PROP_NAME
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.jwt.JWTOptions
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.ext.auth.jwt.jwtAuthOptionsOf
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf
import io.vertx.kotlin.ext.web.client.webClientOptionsOf

suspend fun main() {
  System.getProperties().setProperty(DISABLE_DNS_RESOLVER_PROP_NAME,"true")
  val vertx = Vertx.vertx()
  val webClient = WebClient.create(vertx, webClientOptionsOf(defaultPort = 3000))
  val metabaseApi = MetabaseApi(vertx, webClient)
  val loginMetabase = metabaseApi.loginMetabase("845483926@qq.com", "845483926")

  val id = loginMetabase?.getString("id")
  println("id:$id")

  val randomToken = id?.let { metabaseApi.getRandomToken(it) }

  if (randomToken != null) {
    val token = randomToken.getString("token")
    println("token:$token")
    val tokenCover = metabaseApi.tokenCover(originalValue = "", current = token,sessionId = id)
    println("tokenCover:$tokenCover")
    val genIframeUrl = metabaseApi.genIframeUrl(token, 1, jsonObjectOf())
    println("iframeUrl:$genIframeUrl")
  }
}
