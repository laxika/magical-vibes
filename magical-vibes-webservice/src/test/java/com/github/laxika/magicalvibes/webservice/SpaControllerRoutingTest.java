package com.github.laxika.magicalvibes.webservice;

import org.junit.jupiter.api.Test;
import org.springframework.http.server.PathContainer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The SPA forward has to cover every URL a client can land on directly while leaving the
 * static files served beside it alone. Getting that split wrong fails silently in both
 * directions — a route that is not forwarded 404s only on a hard refresh, and a file that IS
 * forwarded returns {@code index.html} with status 200, so the browser sees a well-formed
 * response full of the wrong bytes and simply discards it. That is how a mapping constraining
 * only its first segment shipped: it answered every {@code /fonts/*.woff2} with HTML, and the
 * only symptom was card text quietly rendering in the fallback typeface.
 *
 * <p>Patterns are read off the annotation rather than restated here, so this cannot drift from
 * the controller — editing the mapping re-runs these expectations against it.
 */
class SpaControllerRoutingTest {

    private static final List<PathPattern> PATTERNS = parsePatterns();

    private static List<PathPattern> parsePatterns() {
        final RequestMapping mapping;
        try {
            mapping = SpaController.class.getMethod("forward").getAnnotation(RequestMapping.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("SpaController.forward() is gone; this test tracks its mapping", e);
        }
        assertThat(mapping).as("SpaController.forward() must carry @RequestMapping").isNotNull();

        final PathPatternParser parser = new PathPatternParser();
        return Arrays.stream(mapping.value()).map(parser::parse).toList();
    }

    private static boolean forwardsToIndex(String path) {
        final PathContainer parsed = PathContainer.parsePath(path);
        return PATTERNS.stream().anyMatch(pattern -> pattern.matches(parsed));
    }

    @Test
    void forwardsEveryClientSideRoute() {
        // Mirrors app.routes.ts — each of these is a URL a player can bookmark or refresh.
        for (String route : List.of("/register", "/home", "/game", "/draft",
                "/cards", "/deck-builder", "/tutorial")) {
            assertThat(forwardsToIndex(route))
                    .as("route %s must reach the Angular app", route)
                    .isTrue();
        }
    }

    @Test
    void leavesStaticFilesAtTheOutputRootAlone() {
        for (String file : List.of("/index.html", "/favicon.ico",
                "/main-ABC123.js", "/styles-ABC123.css")) {
            assertThat(forwardsToIndex(file))
                    .as("%s is a real file and must be served, not forwarded", file)
                    .isFalse();
        }
    }

    @Test
    void leavesStaticFilesInSubdirectoriesAlone() {
        // The regression. These resolved to index.html for as long as the mapping existed;
        // nothing noticed until public/fonts/ became the first asset directory to ship.
        for (String file : List.of("/fonts/cinzel-latin.woff2",
                "/fonts/crimson-text-400-latin.woff2",
                "/fonts/Cinzel-OFL.txt",
                "/assets/img/nested/deeply/sprite.png")) {
            assertThat(forwardsToIndex(file))
                    .as("%s is a real file and must be served, not forwarded", file)
                    .isFalse();
        }
    }

    @Test
    void leavesTheWebSocketEndpointAlone() {
        assertThat(forwardsToIndex("/ws"))
                .as("the WebSocket endpoint must not be forwarded to index.html")
                .isFalse();
    }
}
