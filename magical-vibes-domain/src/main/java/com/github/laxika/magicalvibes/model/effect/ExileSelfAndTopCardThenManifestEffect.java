package com.github.laxika.magicalvibes.model.effect;

/** Exiles the source permanent and the top library card in a shuffled face-down pile, then manifests them. */
public record ExileSelfAndTopCardThenManifestEffect() implements CombatDamageTriggerContextEffect {

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
