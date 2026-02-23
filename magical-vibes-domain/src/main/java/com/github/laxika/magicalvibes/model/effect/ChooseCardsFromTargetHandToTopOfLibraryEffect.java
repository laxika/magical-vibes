package com.github.laxika.magicalvibes.model.effect;

public record ChooseCardsFromTargetHandToTopOfLibraryEffect(int count) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
