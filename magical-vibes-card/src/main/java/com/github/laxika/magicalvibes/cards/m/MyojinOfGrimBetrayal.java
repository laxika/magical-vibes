package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "NEC", collectorNumber = "34")
@CardRegistration(set = "NEC", collectorNumber = "55")
public class MyojinOfGrimBetrayal extends Card {

    public MyojinOfGrimBetrayal() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new CastFromZone(Zone.HAND),
                new EnterWithCountersEffect(CounterType.INDESTRUCTIBLE, new Fixed(1))));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.INDESTRUCTIBLE),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.INDESTRUCTIBLE),
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                                .filter(new CardTypePredicate(CardType.CREATURE))
                                .returnAll(true)
                                .fromAnywhereThisTurn(true)
                                .build()
                ),
                "Remove an indestructible counter from Myojin of Grim Betrayal: Put onto the battlefield under your control all creature cards in all graveyards that were put there from anywhere this turn."
        ));
    }
}
