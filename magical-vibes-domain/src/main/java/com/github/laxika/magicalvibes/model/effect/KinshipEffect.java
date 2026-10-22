package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Kinship (Morningtide): "At the beginning of your upkeep, you may look at the top card of your
 * library. If it shares a creature type with this creature, you may reveal it. If you do,
 * [revealEffects]."
 *
 * <p>Resolves at upkeep: the controller looks at the top card of their library. If that card shares
 * a creature type with the source creature (Changeling counts as every creature type), the
 * controller is offered a "you may reveal" choice; on reveal, {@code revealEffects} resolve against
 * the source permanent. The card stays on top of the library either way. Placed in the
 * {@code UPKEEP_TRIGGERED} slot.</p>
 *
 * <p>When {@code mayLook} is set, the controller is first asked whether to look at the top card at
 * all ("you may look"); {@code lookStep} marks that pending look prompt and is never set on a card.</p>
 */
public record KinshipEffect(List<CardEffect> revealEffects, boolean mayLook, boolean lookStep) implements CardEffect {

    public KinshipEffect(List<CardEffect> revealEffects) {
        this(revealEffects, false, false);
    }

    /** Kinship whose "you may look at the top card" choice is offered before the card is looked at. */
    public static KinshipEffect withLookChoice(List<CardEffect> revealEffects) {
        return new KinshipEffect(revealEffects, true, false);
    }
}
