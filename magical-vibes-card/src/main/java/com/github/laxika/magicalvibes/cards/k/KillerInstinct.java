package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldEffect;

@CardRegistration(set = "GPT", collectorNumber = "120")
public class KillerInstinct extends Card {

    public KillerInstinct() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RevealTopCardCreatureToBattlefieldEffect(true, true));
    }
}
