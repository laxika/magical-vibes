package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent may draw a card" (Soldevi Heretic).
 * <p>
 * The opponent is derived rather than chosen — this engine is two-player, so the sole opponent of
 * the resolving controller is the only legal target. Declaring {@link TargetSpec#NONE} keeps the
 * effect out of the entry's target slot, so it composes with another effect on the same spell or
 * ability that does target (Soldevi Heretic's prevention half targets a creature).
 * <p>
 * {@code TargetOpponentMayDrawCardEffectHandler} queues the optional draw for that opponent, so the
 * "may" is theirs — unlike a plain {@code MayEffect(DrawCardEffect())}, which offers it to the
 * source's controller.
 */
public record TargetOpponentMayDrawCardEffect() implements CardEffect {
}
