package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SlitherborePathway;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "254")
public class DarkborePathway extends Card {

    public DarkborePathway() {
        setBackFaceCard(new SlitherborePathway());
        setModalDoubleFaced(true);

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Darkbore Pathway", List.of()),
                new ChooseOneEffect.ChooseOneOption("Slitherbore Pathway", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "SlitherborePathway";
    }
}
