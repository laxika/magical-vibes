package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

@CardRegistration(set = "OGW", collectorNumber = "60")
public class OathOfJace extends Card {

    public OathOfJace() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardEffect(2, DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ScryEffect(new PermanentCount(
                new PermanentIsPlaneswalkerPredicate(), CountScope.CONTROLLER)));
    }
}
