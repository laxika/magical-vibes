package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "126")
public class GraftedWargear extends Card {

    public GraftedWargear() {
        addStaticEffect(new StaticBoostEffect(3, 2, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.becomesUnattachedFromPermanent(), new SacrificeTriggeringPermanentEffect());
        addActivatedAbility(new EquipActivatedAbility("{0}"));
    }
}
