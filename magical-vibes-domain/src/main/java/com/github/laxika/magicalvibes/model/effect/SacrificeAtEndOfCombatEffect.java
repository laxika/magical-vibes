package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedules the source permanent for sacrifice when combat ends. When {@code damageToController} is
 * greater than zero, the source also deals that much damage to its controller at end of combat
 * ("sacrifice it and it deals N damage to you", Time Elemental). The damage is a delayed triggered
 * ability and happens even if the creature already left the battlefield (last-known information).
 */
public record SacrificeAtEndOfCombatEffect(int damageToController) implements CardEffect {

    public SacrificeAtEndOfCombatEffect() {
        this(0);
    }
}
