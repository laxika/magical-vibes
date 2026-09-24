package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "346")
public class OthelmSigardianOutcast extends Card {

    public OthelmSigardianOutcast() {
        // {2}, {T}: Choose target creature card in your graveyard that was put there from the
        // battlefield this turn. Return it to the battlefield tapped.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .targetGraveyard(true)
                        .targetPutIntoGraveyardFromBattlefieldThisTurn(true)
                        .enterTapped(true)
                        .build()),
                "{2}, {T}: Return target creature card in your graveyard that was put there from the battlefield this turn to the battlefield tapped."
        ));
    }
}
