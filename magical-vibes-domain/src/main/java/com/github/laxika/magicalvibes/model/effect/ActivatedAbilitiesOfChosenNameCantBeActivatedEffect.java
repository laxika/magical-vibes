package com.github.laxika.magicalvibes.model.effect;

public record ActivatedAbilitiesOfChosenNameCantBeActivatedEffect(boolean blocksManaAbilities) implements CardEffect {

    public ActivatedAbilitiesOfChosenNameCantBeActivatedEffect() {
        this(false);
    }
}
