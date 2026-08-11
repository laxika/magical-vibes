package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MagesContestEffect;

@CardRegistration(set = "INV", collectorNumber = "154")
public class MagesContest extends Card {

    public MagesContest() {
        addEffect(EffectSlot.SPELL, new MagesContestEffect());
    }
}
