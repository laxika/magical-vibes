package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "DKA", collectorNumber = "72")
public class ReapTheSeagraf extends Card {

    public ReapTheSeagraf() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.blackZombie(1));
        addCastingOption(new FlashbackCast("{4}{U}"));
    }
}
