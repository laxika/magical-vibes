package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Static replacement: "if a player would draw a card, that player skips that draw instead" (Possessed Portal,
 * {@code eachPlayer()}); "if you would draw a card while your library has no cards in it, skip that draw instead"
 * (Living Conundrum, {@code you()} inside a {@link ConditionalEffect} on the empty library). Read by
 * {@code DrawService} over the active static abilities (none of a face-down or ability-less permanent's), the drawing
 * player judged by {@code SubjectResolutionService.namesPlayer}.
 *
 * @param players whose draws are skipped
 */
public record SkipDrawsEffect(PlayerSubject players) implements CardEffect {

    public SkipDrawsEffect {
        Objects.requireNonNull(players, "players");
    }
}
