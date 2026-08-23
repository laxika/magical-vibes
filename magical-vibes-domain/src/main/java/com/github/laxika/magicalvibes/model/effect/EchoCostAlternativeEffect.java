package com.github.laxika.magicalvibes.model.effect;

/** Capability for a static effect that offers an alternative mana cost for echo abilities. */
public interface EchoCostAlternativeEffect extends CardEffect {

    String alternativeEchoCost();
}
