package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;

import java.util.List;

@CardRegistration(set = "RNA", collectorNumber = "235")
public class Junktroller extends Card {

    public Junktroller() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .targetGraveyard(true)
                        .build()),
                "{T}: Put target card from a graveyard on the bottom of its owner's library."
        ));
    }
}
