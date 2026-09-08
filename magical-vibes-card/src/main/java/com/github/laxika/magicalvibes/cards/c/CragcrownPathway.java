package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TimbercrownPathway;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "261")
public class CragcrownPathway extends Card {

    public CragcrownPathway() {
        setBackFaceCard(new TimbercrownPathway());
        setModalDoubleFaced(true);

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Cragcrown Pathway", List.of()),
                new ChooseOneEffect.ChooseOneOption("Timbercrown Pathway", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "TimbercrownPathway";
    }
}
