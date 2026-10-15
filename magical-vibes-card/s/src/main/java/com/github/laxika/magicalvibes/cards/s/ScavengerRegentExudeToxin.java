package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.ExudeToxin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OmenCast;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;

@CardRegistration(set = "TDM", collectorNumber = "90")
public class ScavengerRegentExudeToxin extends Card {

    public ScavengerRegentExudeToxin() {
        setBackFaceCard(new ExudeToxin());
        addCastingOption(new OmenCast());
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessDiscardsEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "ExudeToxin";
    }
}
