package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SearstepPathway;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "252")
public class BlightstepPathway extends Card {

    public BlightstepPathway() {
        setBackFaceCard(new SearstepPathway());
        setModalDoubleFaced(true);

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Blightstep Pathway", List.of()),
                new ChooseOneEffect.ChooseOneOption("Searstep Pathway", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "SearstepPathway";
    }
}
