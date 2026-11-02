package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "35")
public class LuminousWake extends Card {

    public LuminousWake() {
        setAttackTriggerOwnedByAura(true);
        target(TargetFilters.creature())
                .addTrigger(Triggers.attacks(), new GainLifeEffect(4))
                .addTrigger(Triggers.blocks(), new GainLifeEffect(4));
    }
}
