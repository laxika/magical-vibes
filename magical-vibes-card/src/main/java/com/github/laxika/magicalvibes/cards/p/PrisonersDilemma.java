package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.PrisonersDilemmaEffect;

@CardRegistration(set = "MKC", collectorNumber = "34")
@CardRegistration(set = "MKC", collectorNumber = "344")
public class PrisonersDilemma extends Card {

    public PrisonersDilemma() {
        addEffect(EffectSlot.SPELL, new PrisonersDilemmaEffect());
        addCastingOption(new FlashbackCast("{5}{R}{R}"));
    }
}
