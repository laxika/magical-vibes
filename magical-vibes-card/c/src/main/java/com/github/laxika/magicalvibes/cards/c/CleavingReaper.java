package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "376")
public class CleavingReaper extends Card {

    public CleavingReaper() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayLifeCost(3),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .build()
                ),
                "Pay 3 life: Return this card from your graveyard to your hand. Activate only if you had "
                        + "an Angel or Berserker enter the battlefield under your control this turn."
        ).withActivationCondition(
                new PermanentEnteredThisTurn(new CardAnyOfPredicate(List.of(
                        new CardSubtypePredicate(CardSubtype.ANGEL),
                        new CardSubtypePredicate(CardSubtype.BERSERKER))), 1),
                "Activate only if you had an Angel or Berserker enter the battlefield under your control this turn."));
    }
}
