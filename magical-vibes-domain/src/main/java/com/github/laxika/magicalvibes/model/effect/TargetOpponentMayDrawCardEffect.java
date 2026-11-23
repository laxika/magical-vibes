package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent may draw a card" on an activated ability that also targets a creature (Soldevi Heretic).
 * <p>
 * The opponent is derived rather than chosen: the resolving controller's opponent. Declaring {@link TargetSpec#NONE}
 * keeps the effect out of the entry's target slot, so it composes with the prevention half, which targets a creature.
 * A spell or ability whose only target is the opponent is
 * {@code MayEffect(DrawCardEffect(1, targetPlayerDeclaredElsewhere()), ..., MayChoicePlayer.TARGET_PLAYER)}
 * (Phelddagrif, Tataru Taru).
 * <p>
 * {@code TargetOpponentMayDrawCardEffectHandler} queues the optional draw for that opponent, so the "may" is theirs.
 */
public record TargetOpponentMayDrawCardEffect() implements CardEffect {

    /** The derived opponent stays legal even when the sibling creature target is gone (CR 608.2b). */
    @Override
    public boolean resolvesWhenTargetIllegal() {
        return true;
    }
}
