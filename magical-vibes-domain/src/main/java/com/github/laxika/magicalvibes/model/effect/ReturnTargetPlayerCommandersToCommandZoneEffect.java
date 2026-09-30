package com.github.laxika.magicalvibes.model.effect;

/** Returns every commander controlled by the target player to its owner's command zone. */
public record ReturnTargetPlayerCommandersToCommandZoneEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
