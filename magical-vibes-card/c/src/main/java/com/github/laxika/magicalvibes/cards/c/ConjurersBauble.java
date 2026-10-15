package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "112")
public class ConjurersBauble extends Card {

    public ConjurersBauble() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY)
                                .targetGraveyard(true)
                                .upTo(true)
                                .build(),
                        new DrawCardEffect(1)),
                "{T}, Sacrifice this artifact: Put up to one target card from your graveyard on the bottom of your library. Draw a card."
        ));
    }
}
