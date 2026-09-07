package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static effect that adds damage dealt to opponents of its controller or to
 * permanents those opponents control.
 */
public interface OpponentDamageBonusEffect extends CardEffect {

    int amount();
}
