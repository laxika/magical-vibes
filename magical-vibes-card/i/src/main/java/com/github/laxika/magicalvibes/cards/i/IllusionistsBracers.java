package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "231")
@CardRegistration(set = "RVR", collectorNumber = "260")
public class IllusionistsBracers extends Card {

    public IllusionistsBracers() {
        // Whenever an ability of equipped creature is activated, if it isn't a mana ability, copy
        // that ability. You may choose new targets for the copy.
        addTrigger(Triggers.youActivateNonManaAbility(),
                new CopyControllerActivatedAbilityTriggerEffect(null, null, true));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
