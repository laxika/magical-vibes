package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

@CardRegistration(set = "ODY", collectorNumber = "251")
public class MomentsPeace extends Card {

    public MomentsPeace() {
        addEffect(EffectSlot.SPELL, PreventDamageEffect.allCombat());
        addCastingOption(new FlashbackCast("{2}{G}"));
    }
}
