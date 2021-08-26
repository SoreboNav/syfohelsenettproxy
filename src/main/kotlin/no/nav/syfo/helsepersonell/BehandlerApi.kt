package no.nav.syfo.helsepersonell

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.syfo.log

fun Route.registerBehandlerApi(helsepersonellService: HelsepersonellService) {
    get("/behandler") {
        val fnr = call.request.header("behandlerFnr") ?: run {
            call.respond(HttpStatusCode.BadRequest, "Mangler header `behandlerFnr` med fnr")
            log.warn("Mottatt kall som mangler header behandlerFnr")
            return@get
        }

        when (val behandler = helsepersonellService.finnBehandler(fnr)) {
            null -> call.respond(HttpStatusCode.NotFound, "Fant ikke behandler")
            else -> {
                call.respond(behandler)
            }
        }
    }

    get("/behandlerMedHprNummer") {
        val hprNummer = call.request.header("hprNummer") ?: run {
            call.respond(HttpStatusCode.BadRequest, "Mangler header `hprNummer` med HPR-nummer")
            log.warn("Mottatt kall som mangler header hprNummer")
            return@get
        }

        when (val behandler = helsepersonellService.finnBehandlerFraHprNummer(hprNummer)) {
            null -> call.respond(HttpStatusCode.NotFound, "Fant ikke behandler fra HPR-nummer")
            else -> {
                call.respond(behandler)
            }
        }
    }
}
