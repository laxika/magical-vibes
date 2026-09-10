package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.m.MurkwaterPathway;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "260")
public class ClearwaterPathway extends Card {

    public ClearwaterPathway() {
        setBackFaceCard(new MurkwaterPathway());
        setModalDoubleFaced(true);

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Clearwater Pathway", List.of()),
                new ChooseOneEffect.ChooseOneOption("Murkwater Pathway", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "MurkwaterPathway";
    }
}
