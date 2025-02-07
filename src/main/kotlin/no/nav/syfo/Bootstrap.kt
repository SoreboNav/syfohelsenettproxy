package no.nav.syfo

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.prometheus.client.hotspot.DefaultExports
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeFactory
import no.nav.syfo.application.ApplicationServer
import no.nav.syfo.application.ApplicationState
import no.nav.syfo.application.createApplicationEngine
import no.nav.syfo.helsepersonell.HelsepersonellService
import no.nav.syfo.helsepersonell.helsepersonellV1
import no.nav.syfo.helsepersonell.redis.HelsepersonellRedis
import no.nav.syfo.helsepersonell.redis.createJedisPool
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val objectMapper: ObjectMapper =
    ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

val datatypeFactory: DatatypeFactory = DatatypeFactory.newInstance()

val logger: Logger = LoggerFactory.getLogger("no.nav.syfo.syfohelsenettproxy")

val securelog = LoggerFactory.getLogger("securelog")

fun main() {
    val environment = Environment()
    val serviceUser = ServiceUser()
    val jwkProviderAadV2 =
        JwkProviderBuilder(URL(environment.jwkKeysUrlV2))
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()
    DefaultExports.initialize()
    val applicationState = ApplicationState()

    val jedisPool = createJedisPool()

    val helsepersonellV1 =
        helsepersonellV1(
            environment.helsepersonellv1EndpointURL,
            serviceUser.serviceuserUsername,
            serviceUser.serviceuserPassword
        )

    val helsepersonellService =
        HelsepersonellService(helsepersonellV1, HelsepersonellRedis(jedisPool))

    val applicationEngine =
        createApplicationEngine(
            environment = environment,
            applicationState = applicationState,
            helsepersonellService = helsepersonellService,
            jwkProviderAadV2 = jwkProviderAadV2
        )
    val applicationServer = ApplicationServer(applicationEngine, applicationState)
    applicationServer.start()
}
