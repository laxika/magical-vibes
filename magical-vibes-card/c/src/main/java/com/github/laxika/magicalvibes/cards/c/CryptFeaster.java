package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "FDN", collectorNumber = "59")
public class CryptFeaster extends Card {

    public CryptFeaster() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new GraveyardCardThreshold(7, null),
                new BoostSelfEffect(2, 0)));
    }
}
