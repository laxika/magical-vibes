package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

/**
 * "This creature gains [keyword]." / "This creature loses [keyword]." with no stated duration, so
 * the continuous effect lasts indefinitely (CR 611.2b) — Mist Dragon's two {@code {0}} abilities.
 *
 * <p>The handler records a {@code PERMANENT} floating continuous effect on the source permanent,
 * replacing any earlier one for the same permanent and keyword so the most recent activation wins.
 * {@code GameQueryService.assembleStaticBonus} reads it in layer 6, either adding the keyword
 * ({@code gained == true}) or removing it.
 *
 * @param keyword the keyword gained or lost
 * @param gained  {@code true} to gain the keyword, {@code false} to lose it
 */
public record SetSelfKeywordIndefinitelyEffect(Keyword keyword, boolean gained) implements CardEffect {
}
