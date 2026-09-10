package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PillarvergePathway;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "263")
public class NeedlevergePathway extends Card {

    public NeedlevergePathway() {
        setBackFaceCard(new PillarvergePathway());
        setModalDoubleFaced(true);

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Needleverge Pathway", List.of()),
                new ChooseOneEffect.ChooseOneOption("Pillarverge Pathway", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "PillarvergePathway";
    }
}
