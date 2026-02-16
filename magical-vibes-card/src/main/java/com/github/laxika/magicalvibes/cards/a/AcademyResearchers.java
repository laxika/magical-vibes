package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfEffect;

public class AcademyResearchers extends Card {

    public AcademyResearchers() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(new PutAuraFromHandOntoSelfEffect(), "Put an Aura from your hand onto the battlefield?"));
    }
}
