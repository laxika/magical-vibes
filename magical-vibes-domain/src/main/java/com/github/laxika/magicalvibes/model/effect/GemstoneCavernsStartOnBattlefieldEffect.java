package com.github.laxika.magicalvibes.model.effect;

/** Pregame choice for Gemstone Caverns. */
public record GemstoneCavernsStartOnBattlefieldEffect() implements PregameBattlefieldChoiceEffect {

    @Override
    public boolean onlyForNonStartingPlayer() {
        return true;
    }
}
