package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityTriggerEffect;

@CardRegistration(set = "LRW", collectorNumber = "259")
public class RingsOfBrighthearth extends Card {

    public RingsOfBrighthearth() {
        // Whenever you activate an ability, if it isn't a mana ability, you may pay {2}. If you do,
        // copy that ability. You may choose new targets for the copy.
        addEffect(EffectSlot.ON_CONTROLLER_ACTIVATES_NONMANA_ABILITY,
                new CopyControllerActivatedAbilityTriggerEffect("{2}"));
    }
}
