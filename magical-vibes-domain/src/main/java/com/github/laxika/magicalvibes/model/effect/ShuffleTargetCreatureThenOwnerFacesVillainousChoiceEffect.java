package com.github.laxika.magicalvibes.model.effect;

/** Shuffles a target creature into its owner's library, then its owner faces a villainous choice. */
public record ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect() implements CardEffect {

    public static final String LOSE_LIFE_OPTION = "They lose 5 life";
    public static final String SHUFFLE_CREATURE_OPTION =
            "They shuffle another creature they own into their library";

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(),
                new com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate());
    }
}
