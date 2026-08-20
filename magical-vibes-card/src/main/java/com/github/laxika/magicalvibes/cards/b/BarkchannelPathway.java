package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TidechannelPathway;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "251")
public class BarkchannelPathway extends Card {

    public BarkchannelPathway() {
        setBackFaceCard(new TidechannelPathway());
        setModalDoubleFaced(true);

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Barkchannel Pathway", List.of()),
                new ChooseOneEffect.ChooseOneOption("Tidechannel Pathway", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "TidechannelPathway";
    }
}
