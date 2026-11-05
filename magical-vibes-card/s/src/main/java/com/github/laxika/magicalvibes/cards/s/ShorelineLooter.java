package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "70")
public class ShorelineLooter extends Card {

    public ShorelineLooter() {
        addStaticEffect(new CantBeBlockedEffect());
        addTrigger(Triggers.dealsCombatDamageToPlayer(), SequenceEffect.of(
                new DrawCardEffect(1),
                new ConditionalEffect(
                        new NotCondition(new GraveyardCardThreshold(7, null)),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER))));
    }
}
