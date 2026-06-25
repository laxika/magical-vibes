package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "DKA", collectorNumber = "12")
public class LingeringSouls extends Card {

    public LingeringSouls() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.whiteSpirit(2));
        addCastingOption(new FlashbackCast("{1}{B}"));
    }
}
