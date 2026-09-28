package com.github.laxika.magicalvibes.model.effect;

/** Target opponent chooses between Genesis of the Daleks's two villainous choices. */
public record TargetOpponentFacesGenesisVillainousChoiceEffect() implements CardEffect {

    public static final String DESTROY_DALEKS =
            "Destroy all Dalek creatures and each of your opponents loses life equal to the total power of Daleks that died this turn";
    public static final String DESTROY_NON_DALEKS = "Destroy all non-Dalek creatures";

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
