package com.github.laxika.magicalvibes.model.effect;

/**
 * "As this permanent enters, you get an emblem with …": the controller gets the emblem as a
 * replacement-style entry action, so it exists the moment the permanent enters and never uses the
 * stack. Wraps the {@link CreateEmblemEffect} describing the emblem; only the controller can be the
 * recipient.
 *
 * @param emblem the emblem to create
 */
public record CreateEmblemAsEntersEffect(CreateEmblemEffect emblem) implements ReplacementEffect {
}
