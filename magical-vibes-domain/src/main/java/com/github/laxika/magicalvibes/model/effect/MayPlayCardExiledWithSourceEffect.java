package com.github.laxika.magicalvibes.model.effect;

/** Offers one card exiled with the source permanent for play without paying its mana cost. */
public record MayPlayCardExiledWithSourceEffect() implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
