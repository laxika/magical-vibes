package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LavaglidePathway;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "264")
public class RiverglidePathway extends Card {

    public RiverglidePathway() {
        setBackFaceCard(new LavaglidePathway());
        setModalDoubleFaced(true);

        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Riverglide Pathway", List.of()),
                new ChooseOneEffect.ChooseOneOption("Lavaglide Pathway", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "LavaglidePathway";
    }
}
