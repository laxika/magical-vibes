package com.github.laxika.magicalvibes.model.effect;

/** Static effect that offers an alternative mana cost for echo abilities of permanents controlled by its controller. */
public record AlternativeEchoCostEffect(String manaCost) implements EchoCostAlternativeEffect {

    @Override
    public String alternativeEchoCost() {
        return manaCost;
    }
}
