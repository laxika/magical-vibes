package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect;

@CardRegistration(set = "WOE", collectorNumber = "228")
public class HeartflameDuelist extends Card {

    public HeartflameDuelist() {
        setBackFaceCard(new HeartflameSlash());
        addCastingOption(new AdventureCast("{2}{R}"));
        addEffect(EffectSlot.STATIC, GrantLifelinkToControllerSpellsByColorEffect.allColors());
    }

    @Override
    public String getBackFaceClassName() {
        return "HeartflameSlash";
    }
}
