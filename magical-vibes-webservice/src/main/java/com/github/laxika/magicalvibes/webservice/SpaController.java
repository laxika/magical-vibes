package com.github.laxika.magicalvibes.webservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Hands the Angular router any URL a client can land on directly or refresh, without
 * intercepting the static files served alongside it.
 *
 * <p>Every segment excludes dots, which is what keeps real files out: a bundle, font or
 * image always carries an extension, while a client-side route never does. The previous
 * {@code "/{path:(?!ws)[^\\.]*}/**"} constrained only the FIRST segment and let {@code /**}
 * match the rest dots and all, so anything under a subdirectory — {@code /fonts/x.woff2} —
 * was answered with {@code index.html}. Nothing caught it for as long as it existed because
 * Angular emits its bundles at the output root, where there is no second segment to match;
 * {@code public/fonts/} was the first asset directory this app shipped, and the browser
 * quietly fell back to Georgia rather than reporting the HTML it got instead of a font.
 *
 * <p>Depth is enumerated rather than left open because a regex cannot constrain the segments
 * behind {@code /**}, and {@code PathPattern}'s capture-all {@code {*path}} takes no regex.
 * Three is headroom: every route in {@code app.routes.ts} is a single segment. A deeper route
 * added later needs another line here, or it will 404 on a hard refresh.
 *
 * <p>The {@code (?!ws)} guard keeps the WebSocket endpoint out of the forward.
 */
@Controller
public class SpaController {

    @RequestMapping(value = {
            "/{p1:(?!ws)[^\\.]*}",
            "/{p1:(?!ws)[^\\.]*}/{p2:[^\\.]*}",
            "/{p1:(?!ws)[^\\.]*}/{p2:[^\\.]*}/{p3:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
