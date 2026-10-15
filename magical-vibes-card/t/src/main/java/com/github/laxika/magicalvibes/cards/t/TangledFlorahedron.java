package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "211")
public class TangledFlorahedron extends Card {

    public TangledFlorahedron() {
        setBackFaceCard(new TangledVale());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Tangled Florahedron", List.of()),
                new ChooseOneEffect.ChooseOneOption("Tangled Vale", List.of())
        )));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }

    @Override
    public String getBackFaceClassName() {
        return "TangledVale";
    }
}
