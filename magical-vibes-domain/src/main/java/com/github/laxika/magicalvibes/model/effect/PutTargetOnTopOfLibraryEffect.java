package com.github.laxika.magicalvibes.model.effect;

public record PutTargetOnTopOfLibraryEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
