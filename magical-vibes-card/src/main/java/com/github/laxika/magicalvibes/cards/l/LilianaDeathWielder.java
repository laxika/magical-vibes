package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "275")
public class LilianaDeathWielder extends Card {

    public LilianaDeathWielder() {
        // +2: Put a -1/-1 counter on up to one target creature.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1)),
                "+2: Put a -1/-1 counter on up to one target creature.",
                TargetFilters.creature(),
                +2, null, null,
                List.of(), 0, 1
        ));

        // −3: Destroy target creature with a -1/-1 counter on it.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DestroyTargetPermanentEffect()),
                "\u22123: Destroy target creature with a -1/-1 counter on it.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE)
                        )),
                        "Target must be a creature with a -1/-1 counter on it"
                )
        ));

        // −10: Return all creature cards from your graveyard to the battlefield.
        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .returnAll(true)
                        .build()),
                "\u221210: Return all creature cards from your graveyard to the battlefield."
        ));
    }
}
