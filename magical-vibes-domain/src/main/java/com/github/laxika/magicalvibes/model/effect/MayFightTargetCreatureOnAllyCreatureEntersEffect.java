package com.github.laxika.magicalvibes.model.effect;

/** Marker stored in an emblem for an entering creature to optionally fight a target creature. */
public interface MayFightTargetCreatureOnAllyCreatureEntersEffect extends CardEffect {

    record Marker() implements MayFightTargetCreatureOnAllyCreatureEntersEffect {
    }
}
