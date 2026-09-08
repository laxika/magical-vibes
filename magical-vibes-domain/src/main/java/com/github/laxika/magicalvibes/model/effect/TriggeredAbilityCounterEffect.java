package com.github.laxika.magicalvibes.model.effect;

/** Static capability for permanents that counter triggered abilities caused by permanents entering. */
public interface TriggeredAbilityCounterEffect extends CardEffect {

    int counterCost();
}
