package com.github.laxika.magicalvibes.model.effect;

public record UnattachEquipmentFromTargetPermanentsEffect() implements CardEffect {
    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
