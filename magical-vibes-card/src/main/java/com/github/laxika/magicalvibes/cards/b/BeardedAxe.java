package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.Set;

@CardRegistration(set = "KHM", collectorNumber = "388")
public class BeardedAxe extends Card {

    public BeardedAxe() {
        PermanentCount dwarvesEquipmentAndVehicles = new PermanentCount(
                new PermanentHasAnySubtypePredicate(Set.of(
                        CardSubtype.DWARF, CardSubtype.EQUIPMENT, CardSubtype.VEHICLE)),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new AttachedBoostEffect(
                dwarvesEquipmentAndVehicles, dwarvesEquipmentAndVehicles, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
