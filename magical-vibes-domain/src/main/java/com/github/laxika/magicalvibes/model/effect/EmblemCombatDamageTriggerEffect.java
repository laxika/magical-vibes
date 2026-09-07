package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for emblem abilities that trigger when one or more creatures controlled by the emblem's
 * controller deal combat damage to an opponent.
 */
public interface EmblemCombatDamageTriggerEffect extends CardEffect {

    int lifeThreshold();

    CardEffect triggeredEffect();
}
