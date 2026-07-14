package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "7ED", collectorNumber = "16")
public class GerrardsWisdom extends Card {

    public GerrardsWisdom() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(
                new Scaled(new CardsInHand(CountScope.CONTROLLER), 2)));
    }
}
