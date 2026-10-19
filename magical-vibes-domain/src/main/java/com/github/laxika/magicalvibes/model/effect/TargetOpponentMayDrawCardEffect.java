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
public record TargetOpponentMayDrawCardEffect(boolean chosenTarget) implements CardEffect {

    /** Derived-opponent form: the sole opponent is the "target" and is never chosen. */
    public TargetOpponentMayDrawCardEffect() {
        this(false);
    }

    /**
     * Chosen-target form ({@code chosenTarget = true}): the card binds this effect to a real opponent
     * target via {@code target(...).addEffect(...)}, so the ability can't be put on the stack when no
     * opponent can be targeted and does nothing if that target becomes illegal.
     */
    public static TargetOpponentMayDrawCardEffect ofChosenTarget() {
        return new TargetOpponentMayDrawCardEffect(true);
    }

    /** The derived opponent stays legal even when the sibling creature target is gone (CR 608.2b). */
    @Override
    public boolean resolvesWhenTargetIllegal() {
        return !chosenTarget;
    }
}
