package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "116")
public class ForcemageAdvocate extends Card {

    public ForcemageAdvocate() {
        ReturnCardFromGraveyardEffect returnCard = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .source(GraveyardSearchScope.OPPONENT_GRAVEYARD)
                .targetGraveyard(true)
                .targetGroup(0)
                .build();

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(returnCard, new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                "{T}: Return target card from an opponent's graveyard to their hand. Put a +1/+1 counter on target creature.",
                List.of(
                        new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.OPPONENT_GRAVEYARD),
                        TargetFilters.creature()),
                2,
                2));
    }
}
