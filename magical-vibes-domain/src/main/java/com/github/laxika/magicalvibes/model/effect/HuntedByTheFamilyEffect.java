package com.github.laxika.magicalvibes.model.effect;

/** Resolves Hunted by The Family's per-target villainous choice. */
public record HuntedByTheFamilyEffect() implements CardEffect {

    public static final String HUMAN_OPTION =
            "That creature becomes a 1/1 white Human creature and loses all abilities";
    public static final String COPY_OPTION = "You create a token that's a copy of it";

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
