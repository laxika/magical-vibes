package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelveCost;

@CardRegistration(set = "KTK", collectorNumber = "137")
public class HootingMandrills extends Card {

    public HootingMandrills() {
        addEffect(EffectSlot.SPELL, new DelveCost());
    }
}
