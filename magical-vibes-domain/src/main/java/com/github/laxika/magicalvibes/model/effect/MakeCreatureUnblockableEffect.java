package com.github.laxika.magicalvibes.model.effect;

public record MakeCreatureUnblockableEffect(boolean selfTargeting) implements CardEffect {

    public MakeCreatureUnblockableEffect() {
        this(false);
    }

    @Override
    public boolean canTargetPermanent() {
        return !selfTargeting;
    }

    @Override
    public boolean isSelfTargeting() {
        return selfTargeting;
    }
}
