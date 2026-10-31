package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "39")
@CardRegistration(set = "PIO", collectorNumber = "45")
public class AqueousForm extends Card {

    public AqueousForm() {
        // The attack trigger is the Aura's own ability, so its controller is the Aura's controller even
        // when the enchanted creature is an opponent's.
        setAttackTriggerOwnedByAura(true);

        target(TargetFilters.creature())
                .addStaticEffect(new CantBeBlockedEffect())
                .addEffect(EffectSlot.ON_ATTACK, new ScryEffect(1));
    }
}
