package com.github.laxika.magicalvibes.model.effect;

public record PutTargetOnBottomOfLibraryEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
