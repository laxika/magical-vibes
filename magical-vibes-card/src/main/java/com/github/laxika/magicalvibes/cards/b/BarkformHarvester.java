package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "243")
public class BarkformHarvester extends Card {

    public BarkformHarvester() {
        // {2}: Put target card from your graveyard on the bottom of your library.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY)
                        .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                        .targetGraveyard(true)
                        .build()),
                "{2}: Put target card from your graveyard on the bottom of your library."
        ));
    }
}
