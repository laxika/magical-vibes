package com.github.laxika.magicalvibes.model.effect;

/** Marks a trigger effect that fires once for a simultaneous artifact-or-creature death event. */
public interface BatchedArtifactOrCreatureDeathTriggerEffect extends CardEffect {

    CardEffect wrapped();
}
