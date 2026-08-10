package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "EXO", collectorNumber = "73")
public class ScareTactics extends Card {

    public ScareTactics() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 0));
    }
}
