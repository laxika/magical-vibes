package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.NonbasicLandsProduceFixedManaColorUntilEndOfTurnEffect;

@CardRegistration(set = "NEM", collectorNumber = "36")
public class PaleMoon extends Card {

    public PaleMoon() {
        addEffect(EffectSlot.SPELL,
                new NonbasicLandsProduceFixedManaColorUntilEndOfTurnEffect(ManaColor.COLORLESS));
    }
}
