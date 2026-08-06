package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;

@CardRegistration(set = "GTC", collectorNumber = "231")
public class IllusionistsBracers extends Card {

    public IllusionistsBracers() {
        // Whenever an ability of equipped creature is activated, if it isn't a mana ability, copy
        // that ability. You may choose new targets for the copy.
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
                new CopyControllerActivatedAbilityTriggerEffect(null, null, true));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
