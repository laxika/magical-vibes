package com.github.laxika.magicalvibes.model.effect;

public record EquipEffect() implements CardEffect {
    @Override public boolean canTargetPermanent() { return true; }
}
