package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "241")
public class TheDeckOfManyThings extends Card {

    public TheDeckOfManyThings() {
        ReturnCardFromGraveyardEffect reanimateCreature = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .mandatory(true)
                .grantOnDeathEffect(new TargetPlayerLosesGameEffect(null))
                .build();

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(RollD20Effect.withSubtractedAmount(
                        new CardsInHand(CountScope.CONTROLLER),
                        new DiscardHandEffect(),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.HAND)
                                .returnAtRandom(true)
                                .build(),
                        new DrawCardEffect(2),
                        reanimateCreature)),
                "{2}, {T}: Roll a d20 and subtract the number of cards in your hand."
        ));
    }
}
