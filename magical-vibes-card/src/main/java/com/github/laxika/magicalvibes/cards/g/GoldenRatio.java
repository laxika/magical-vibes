package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DistinctPowersAmongControlledCreatures;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "STX", collectorNumber = "190")
public class GoldenRatio extends Card {

    public GoldenRatio() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new DistinctPowersAmongControlledCreatures()));
    }
}
