package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPowerAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "90")
public class AleshaWhoSmilesAtDeath extends Card {

    public AleshaWhoSmilesAtDeath() {
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect(
                "{W/B}{W/B}",
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardPowerAtMostPredicate(2)
                        )))
                        .targetGraveyard(true)
                        .enterTapped(true)
                        .enterAttacking(true)
                        .build(),
                "Pay {W/B}{W/B} to return target creature card with power 2 or less from your graveyard to the battlefield tapped and attacking?"
        ));
    }
}
