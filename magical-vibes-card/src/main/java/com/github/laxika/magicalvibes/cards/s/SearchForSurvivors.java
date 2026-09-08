package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

@CardRegistration(set = "PCY", collectorNumber = "102")
public class SearchForSurvivors extends Card {

    public SearchForSurvivors() {
        addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .returnAtRandom(true)
                .battlefieldIfCreatureElseExile(true)
                .shuffleGraveyardBeforeRandomSelection(true)
                .build());
    }
}
