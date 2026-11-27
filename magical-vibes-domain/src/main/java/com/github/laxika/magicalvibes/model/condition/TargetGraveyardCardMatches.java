package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * The card targeted in a graveyard matches {@code filter} as the effect resolves; false once the card has left the
 * graveyard. Gates one of several returns of the same target by what the card is (Brilliance Unleashed: "Return it to
 * the battlefield if it's an artifact creature card. Otherwise, return it ... and it's a 3/3 Robot").
 *
 * @param filter      what the targeted card must be
 * @param description the card description used in log messages ("an artifact creature card")
 */
public record TargetGraveyardCardMatches(CardPredicate filter, String description) implements Condition {

    public TargetGraveyardCardMatches {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(description, "description");
    }

    @Override
    public String conditionName() {
        return "target graveyard card is " + description;
    }

    @Override
    public String conditionNotMetReason() {
        return "target graveyard card isn't " + description;
    }
}
