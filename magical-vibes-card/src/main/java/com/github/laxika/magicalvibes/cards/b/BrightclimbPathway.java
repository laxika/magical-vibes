package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "259")
public class BrightclimbPathway extends Card {

    public BrightclimbPathway() {
        setBackFaceCard(new GrimclimbPathway());
        setModalDoubleFaced(true);

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Brightclimb Pathway", List.of()),
                new ChooseOneEffect.ChooseOneOption("Grimclimb Pathway", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "GrimclimbPathway";
    }
}
