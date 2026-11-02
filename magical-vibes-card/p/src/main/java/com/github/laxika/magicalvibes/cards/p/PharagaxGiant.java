package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BNG", collectorNumber = "104")
public class PharagaxGiant extends Card {

    public PharagaxGiant() {
        addStaticEffect(new TributeEffect(2));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new TributeNotPaidEffect(
                new DealDamageToPlayersEffect(5, DamageRecipient.EACH_OPPONENT)));
    }
}
