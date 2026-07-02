package com.github.laxika.magicalvibes.model.condition;

/**
 * The entering permanent's name matches the source permanent's imprinted card name
 * (Invader Parasite). Evaluated at trigger-collection time against the entering card.
 */
public record ImprintedCardNameMatchesEnteringPermanent() implements Condition {

    @Override
    public String conditionName() {
        return "imprinted card name matches";
    }

    @Override
    public String conditionNotMetReason() {
        return "entering permanent name does not match imprinted card";
    }
}
