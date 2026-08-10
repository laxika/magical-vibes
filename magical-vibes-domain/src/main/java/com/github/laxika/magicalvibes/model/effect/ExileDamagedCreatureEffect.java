package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for a damage trigger that exiles the creature dealt damage.
 *
 * <p>The default form is self-scoped: "Whenever this creature deals damage to a creature, exile
 * that creature." The equipment-scoped form is used by Sword of Kaldra.
 */
public record ExileDamagedCreatureEffect(boolean equipmentScoped) implements DamagedCreatureTriggerEffect {

    public ExileDamagedCreatureEffect() {
        this(false);
    }

    @Override
    public CardEffect triggeredEffect() {
        return new ExileTargetPermanentEffect();
    }
}
