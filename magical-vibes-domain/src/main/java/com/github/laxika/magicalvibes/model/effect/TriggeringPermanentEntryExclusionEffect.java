package com.github.laxika.magicalvibes.model.effect;

/** Capability for an effect whose entering permanent must remember its source permanent. */
public interface TriggeringPermanentEntryExclusionEffect extends CardEffect {

    boolean suppressesTriggeringPermanentEntry();
}
