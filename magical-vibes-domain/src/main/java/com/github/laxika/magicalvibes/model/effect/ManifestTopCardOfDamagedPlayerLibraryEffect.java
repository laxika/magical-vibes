package com.github.laxika.magicalvibes.model.effect;

/** Manifests the top card of the combat-damaged player's library under the effect controller's control. */
public record ManifestTopCardOfDamagedPlayerLibraryEffect() implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
