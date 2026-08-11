package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "KTK", collectorNumber = "59")
public class TreasureCruise extends Card {

    public TreasureCruise() {
        addEffect(EffectSlot.SPELL, new DelveCost());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
