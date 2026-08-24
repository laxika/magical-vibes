package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "9")
public class Chronosavant extends Card {

    public Chronosavant() {
        // {1}{W}: Return this card from your graveyard to the battlefield tapped.
        // You skip your next turn.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterTapped(true)
                                .build(),
                        new SkipNextEffect(SkipKind.TURN)
                ),
                "{1}{W}: Return this card from your graveyard to the battlefield tapped. You skip your next turn."
        ));
    }
}
