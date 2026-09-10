package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "180")
public class BalaGedRecovery extends Card {

    public BalaGedRecovery() {
        setBackFaceCard(new BalaGedSanctuary());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Bala Ged Recovery",
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .targetGraveyard(true)
                                .build()),
                new ChooseOneEffect.ChooseOneOption("Bala Ged Sanctuary", List.of())
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "BalaGedSanctuary";
    }
}
