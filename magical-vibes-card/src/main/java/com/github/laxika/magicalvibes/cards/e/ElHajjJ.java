package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "4ED", collectorNumber = "134")
public class ElHajjJ extends Card {

    public ElHajjJ() {
        // Whenever this creature deals damage, you gain that much life.
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, new GainLifeEffect(new EventValue()));
    }
}
