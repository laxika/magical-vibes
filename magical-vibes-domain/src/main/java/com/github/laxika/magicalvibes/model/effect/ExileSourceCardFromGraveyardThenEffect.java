package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the source card from its graveyard and resolves the payload only if the exile succeeds
 * ("exile it. If you do, ...").
 *
 * <p>By default the payload is a follow-up reflexive trigger: it deliberately does not contribute
 * its targets to this effect, and its targets are chosen after the optional exile is accepted and
 * completed. With {@link #sameAbility(CardEffect)} the payload is instead part of the same ability:
 * it contributes its targets (chosen as the trigger goes on the stack) and resolves against the same
 * stack entry once the exile has happened (Iname, Life Aspect).</p>
 *
 * @param thenEffect the payload of the reflexive trigger or of the same ability
 * @param reflexive  whether the payload is put on the stack as a separate reflexive trigger
 */
public record ExileSourceCardFromGraveyardThenEffect(CardEffect thenEffect, boolean reflexive) implements CardEffect {

    public ExileSourceCardFromGraveyardThenEffect(CardEffect thenEffect) {
        this(thenEffect, true);
    }

    /** The payload resolves as part of the same ability, with targets chosen when it was put on the stack. */
    public static ExileSourceCardFromGraveyardThenEffect sameAbility(CardEffect thenEffect) {
        return new ExileSourceCardFromGraveyardThenEffect(thenEffect, false);
    }

    public ExileSourceCardFromGraveyardThenEffect {
        if (thenEffect == null) {
            throw new IllegalArgumentException("ExileSourceCardFromGraveyardThenEffect requires a payload");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return reflexive ? TargetSpec.NONE : thenEffect.targetSpec();
    }
}
