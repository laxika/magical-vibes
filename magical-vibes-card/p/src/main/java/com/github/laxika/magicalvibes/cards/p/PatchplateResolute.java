package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YBRO", collectorNumber = "28")
public class PatchplateResolute extends Card {

    public PatchplateResolute() {
        var boon = RegisterDelayedControllerSpellCastTriggerEffect.oneShotUntilConsumed(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(
                        new Fixed(1))));
        addTrigger(Triggers.enters(ObjectRelation.THIS), boon);
        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), boon);
        addUnearth("{1}{W}");
    }
}
