package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantHandActivatedAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "23")
@CardRegistration(set = "WHO", collectorNumber = "342")
@CardRegistration(set = "WHO", collectorNumber = "628")
@CardRegistration(set = "WHO", collectorNumber = "933")
public class JoGrant extends Card {

    public JoGrant() {
        addTrigger(Triggers.youCycleCard(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        ActivatedAbility cycling = new ActivatedAbility(false, "{2}{W}",
                List.of(new DrawCardEffect(1)),
                "Cycling {2}{W} ({2}{W}, Discard this card: Draw a card.)");
        addStaticEffect(new GrantHandActivatedAbilityToCardsEffect(
                cycling, new CardIsHistoricPredicate(), true));
    }
}
