package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "37")
public class SejiriShelter extends Card {

    public SejiriShelter() {
        setBackFaceCard(new SejiriGlacier());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Target creature you control gains protection from the color of your choice until end of turn",
                        new GrantProtectionChoiceUntilEndOfTurnEffect(),
                        TargetFilters.creatureYouControl()),
                new ChooseOneEffect.ChooseOneOption("Sejiri Glacier", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "SejiriGlacier";
    }
}
