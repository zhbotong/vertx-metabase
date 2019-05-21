package cc.shallow.vertx_metabase

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendJsonObjectAwait
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import io.vertx.ext.jwt.JWTOptions
import io.vertx.kotlin.ext.auth.jwt.jwtAuthOptionsOf
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf


class MetabaseApi(private val vertx: Vertx, private val webClient: WebClient) : CoroutineScope {

  private val metabaseSiteUrl = "http://127.0.0.1:3000"

  private val loginUrl = "/api/session"

  private val randoomTokenUrl = "/api/util/random_token"

  private val tokenCoverUrl = "/api/setting/embedding-secret-key"

  override val coroutineContext: CoroutineContext by lazy { vertx.dispatcher() }


  /**
   * login metabase
   * @param username
   * @param passsword
   * @return {"id":"xxxxx-xxxxxx-xxxxx"}
   */
  suspend fun loginMetabase(username: String, passsword: String): JsonObject? {
    val loginParam = jsonObjectOf("username" to username, "password" to passsword)
    return webClient
      .post(loginUrl)
      .sendJsonObjectAwait(loginParam)
      .bodyAsJsonObject()
  }

  /**
   * @param login return id
   * @return {"token":"xxxx-xxxx"}
   */
  suspend fun getRandomToken(sessionId: String): JsonObject? {
    return webClient.get(randoomTokenUrl)
      .putHeader("Cookie", "metabase.SESSION=$sessionId")
      .sendAwait().bodyAsJsonObject()
  }


  /**
   * @param originalValue
   * @param current
   * @param sessionId
   * @return xxxxxxxxxxxxxx
   */
  suspend fun tokenCover(originalValue: String?, current: String,sessionId: String): String? {
    val param = jsonObjectOf(
      "env_name" to "MB_EMBEDDING_SECRET_KEY",
      "is_env_setting" to false,
      "value" to current,
      "originalValue" to originalValue
    )
    return webClient.put(tokenCoverUrl)
      .putHeader("Cookie", "metabase.SESSION=$sessionId")
      .sendJsonObjectAwait(param)
      .bodyAsString()
  }

  /**
   * genIframeUrl
   *
   * @param token METABASE_SECRET_KEY (getRandomToken)
   * @param dashboard
   * @param param
   */
  suspend fun genIframeUrl(token: String, dashboard: Int, param: JsonObject): String {
    val resource = jsonObjectOf("dashboard" to dashboard)
    val result = jsonObjectOf("resource" to resource, "params" to param)
    val jwt = JWTAuth.create(
      vertx, jwtAuthOptionsOf(
        pubSecKeys = listOf(
          pubSecKeyOptionsOf(
            symmetric = true, algorithm = "HS256",
            publicKey = token
          )
        )
      )
    )
    val generateToken = jwt.generateToken(result, JWTOptions())
    return "$metabaseSiteUrl/embed/dashboard/$generateToken#bordered=true&titled=true"
  }
}
