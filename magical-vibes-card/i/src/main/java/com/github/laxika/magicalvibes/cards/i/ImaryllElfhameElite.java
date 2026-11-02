package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AssignCombatDamageAsThoughUnblockedEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GN3", collectorNumber = "5")
public class ImaryllElfhameElite extends Card {

    public ImaryllElfhameElite() {
        PermanentCount otherElvesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER, true);
        addTrigger(Triggers.attacks(), new BoostSelfEffect(otherElvesYouControl, otherElvesYouControl));
        addStaticEffect(new AssignCombatDamageAsThoughUnblockedEffect());
    }
}
