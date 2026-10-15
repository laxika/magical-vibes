package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "102")
@CardRegistration(set = "2XM", collectorNumber = "84")
public class DireFleetHoarder extends Card {

    public DireFleetHoarder() {
        addEffect(EffectSlot.ON_DEATH, CreateTokenEffect.ofTreasureToken(1));
    }
}
