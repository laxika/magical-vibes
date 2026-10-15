package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "40")
public class SkyclaveCleric extends Card {

    public SkyclaveCleric() {
        setBackFaceCard(new SkyclaveBasilica());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Skyclave Cleric", List.of()),
                new ChooseOneEffect.ChooseOneOption("Skyclave Basilica", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "SkyclaveBasilica";
    }
}
