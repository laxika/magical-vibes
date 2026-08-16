package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "262")
@CardRegistration(set = "M21", collectorNumber = "230")
public class EpitaphGolem extends Card {

    public EpitaphGolem() {
        // {2}: Put target card from your graveyard on the bottom of your library.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY)
                        .targetGraveyard(true)
                        .build()),
                "{2}: Put target card from your graveyard on the bottom of your library."
        ));
    }
}
