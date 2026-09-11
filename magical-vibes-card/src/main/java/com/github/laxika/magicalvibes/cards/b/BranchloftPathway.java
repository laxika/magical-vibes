package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "258")
public class BranchloftPathway extends Card {

    public BranchloftPathway() {
        setBackFaceCard(new BoulderloftPathway());
        setModalDoubleFaced(true);

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Branchloft Pathway", List.of()),
                new ChooseOneEffect.ChooseOneOption("Boulderloft Pathway", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "BoulderloftPathway";
    }
}
