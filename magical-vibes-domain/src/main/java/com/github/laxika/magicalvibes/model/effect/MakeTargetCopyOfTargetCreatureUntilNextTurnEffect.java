package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Makes a target permanent (targetIds[0], typically a Shapeshifter) become a copy of a
 * target creature (targetIds[1]) until the controller's next turn. Used by Shapesharer.
 *
 * <p>It is also the payload of every copy effect that lasts until its controller's next turn (layer 1, CR 613.1a):
 * a floating effect on the copying permanent with {@code Duration.UNTIL_YOUR_NEXT_TURN}, carrying
 * {@code copiedCard}, the card the permanent shows while this copy is the newest one, and {@code preCopyCard}, the
 * card it showed before the first of its active copies of this kind. As one ends, the newest one left applies again
 * (CR 613.7), or, with none left, the permanent shows {@code preCopyCard} again. Both are {@code null} on the card's
 * own ability.
 */
public record MakeTargetCopyOfTargetCreatureUntilNextTurnEffect(Card copiedCard, Card preCopyCard)
        implements CardEffect {

    public MakeTargetCopyOfTargetCreatureUntilNextTurnEffect() {
        this(null, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}
