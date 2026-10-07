package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandBottomThenDealManaValueDamageEffect;

@CardRegistration(set = "MH2", collectorNumber = "118")
public class CalibratedBlast extends Card {

    public CalibratedBlast() {
        addEffect(EffectSlot.SPELL, RevealUntilNonlandBottomThenDealManaValueDamageEffect.reflexiveAnyTarget(true));
        addCastingOption(new FlashbackCast("{3}{R}{R}"));
    }
}
