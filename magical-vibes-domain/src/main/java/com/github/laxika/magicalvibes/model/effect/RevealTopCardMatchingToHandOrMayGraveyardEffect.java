package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Reveals the top card, puts matching cards into hand, and may put other cards into the graveyard. */
public record RevealTopCardMatchingToHandOrMayGraveyardEffect(
        CardPredicate matchPredicate,
        Stage stage
) implements CardEffect {

    public enum Stage {
        REVEAL,
        MAY_GRAVEYARD
    }

    public RevealTopCardMatchingToHandOrMayGraveyardEffect(CardPredicate matchPredicate) {
        this(matchPredicate, Stage.REVEAL);
    }

    public RevealTopCardMatchingToHandOrMayGraveyardEffect withStage(Stage nextStage) {
        return new RevealTopCardMatchingToHandOrMayGraveyardEffect(matchPredicate, nextStage);
    }
}
