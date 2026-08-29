package com.github.laxika.magicalvibes.model.effect;

public record CreateTokenForEmergeSacrificeEffect(CreateTokenEffect tokenEffect) implements CardEffect {

    @Override
    public boolean onlyTriggersOnSacrifice() {
        return true;
    }
}
