package com.github.laxika.magicalvibes.model.effect;

/** Target opponent chooses between discarding three cards and the controller casting a hand spell free. */
public record GreatIntelligencesPlanVillainousChoiceEffect() implements CardEffect {

    public static final String DISCARD_OPTION = "They discard three cards";
    public static final String CAST_OPTION = "You may cast a spell from your hand without paying its mana cost";

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
