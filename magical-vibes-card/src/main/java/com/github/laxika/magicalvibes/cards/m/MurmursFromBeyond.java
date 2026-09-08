package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MurmursFromBeyondEffect;

@CardRegistration(set = "SOK", collectorNumber = "47")
public class MurmursFromBeyond extends Card {

    public MurmursFromBeyond() {
        addEffect(EffectSlot.SPELL, new MurmursFromBeyondEffect());
    }
}
