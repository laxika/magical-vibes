package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "ISD", collectorNumber = "109")
public class MoanOfTheUnhallowed extends Card {

    public MoanOfTheUnhallowed() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.blackZombie(2));
        addCastingOption(new FlashbackCast("{5}{B}{B}"));
    }
}
