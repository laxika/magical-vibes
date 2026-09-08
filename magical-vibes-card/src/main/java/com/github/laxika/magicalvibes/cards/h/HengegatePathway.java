package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MistgatePathway;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "260")
public class HengegatePathway extends Card {

    public HengegatePathway() {
        setBackFaceCard(new MistgatePathway());
        setModalDoubleFaced(true);

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Hengegate Pathway", List.of()),
                new ChooseOneEffect.ChooseOneOption("Mistgate Pathway", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "MistgatePathway";
    }
}
