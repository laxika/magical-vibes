package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.layer.CharacteristicState;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;

/**
 * Shared check for auras that give an enchanted Equipment an ability that affects its equipped
 * creature (Rune of Might's "Enchanted Equipment has 'Equipped creature gets +1/+1 and has trample'").
 */
final class EnchantedEquipmentGrantSupport {

    private EnchantedEquipmentGrantSupport() {
    }

    /**
     * Whether the Equipment still has the ability the aura granted it. An effect that made the
     * Equipment lose all abilities after the aura's timestamp (Merfolk Trickster) removes the granted
     * ability too (CR 613.7). A one-shot "until end of turn" loss is read from the permanent's flag,
     * because the layered state may not have replayed it yet when the ability is applied.
     */
    static boolean equipmentKeepsGrantedAbility(Permanent aura, Permanent equipment) {
        CharacteristicState state = LayerSystemService.activeStateFor(equipment.getId());
        if (state != null && state.isLosesAllAbilities()
                && state.getLosesAllAbilitiesTimestamp() > aura.getTimestamp()) {
            return false;
        }
        return !equipment.isLosesAllAbilitiesUntilEndOfTurn();
    }
}
