package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "129")
@CardRegistration(set = "GK2", collectorNumber = "71")
public class RakdosTheDefiler extends Card {

    public RakdosTheDefiler() {
        var nonDemon = new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.DEMON));
        addTrigger(Triggers.attacks(), new SacrificePermanentsEffect(
                new HalvedRoundedUp(new PermanentCount(nonDemon, CountScope.CONTROLLER)),
                nonDemon, SacrificeRecipient.CONTROLLER));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new SacrificePermanentsEffect(
                new HalvedRoundedUp(new PermanentCount(nonDemon, CountScope.TARGET_PLAYER)),
                nonDemon, SacrificeRecipient.TARGET_PLAYER));
    }
}
