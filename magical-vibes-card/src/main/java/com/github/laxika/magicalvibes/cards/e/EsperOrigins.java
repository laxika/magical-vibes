package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SummonEsperMaduin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ExileSpellAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "FIN", collectorNumber = "185")
public class EsperOrigins extends Card {

    public EsperOrigins() {
        setBackFaceCard(new SummonEsperMaduin());

        addEffect(EffectSlot.SPELL, new SurveilEffect(2));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
        addEffect(EffectSlot.SPELL, new ExileSpellAndReturnTransformedEffect());
        addCastingOption(new FlashbackCast("{3}{G}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "SummonEsperMaduin";
    }
}
