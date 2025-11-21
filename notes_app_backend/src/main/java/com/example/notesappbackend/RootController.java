package com.example.notesappbackend;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * RootController handles the application root and provides
 * a friendly redirect to Swagger UI documentation.
 *
 * This controller coexists with HelloController by delegating "/" to /docs
 * to avoid duplicate welcome text and to reduce chances of a 404 Whitelabel page.
 */
@Controller
@Tag(name = "Root", description = "Root and documentation redirects")
public class RootController {

    /**
     * PUBLIC_INTERFACE
     * GET / -> redirects to /docs, which then redirects to Swagger UI while preserving scheme/host/port.
     *
     * For browser requests, this prevents Whitelabel Error Page and surfaces the API docs immediately.
     *
     * @param request current HTTP request
     * @return RedirectView to /docs
     */
    @GetMapping(value = "/", produces = { MediaType.TEXT_HTML_VALUE, MediaType.ALL_VALUE })
    @Operation(summary = "Root redirect", description = "Redirects the application root to the API docs page")
    public RedirectView root(HttpServletRequest request) {
        String target = ServletUriComponentsBuilder
                .fromRequest(request)
                .replacePath("/docs")
                .replaceQuery(null)
                .build()
                .toUriString();

        RedirectView rv = new RedirectView(target);
        rv.setHttp10Compatible(false);
        return rv;
    }
}
