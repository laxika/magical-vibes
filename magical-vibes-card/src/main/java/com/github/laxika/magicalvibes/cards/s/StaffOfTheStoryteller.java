package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AA3", collectorNumber = "4")
public class StaffOfTheStoryteller extends Card {

    public StaffOfTheStoryteller() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSpirit(1));

        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardTypePredicate(CardType.CREATURE),
                        new PutCountersOnSelfEffect(CounterType.STORY)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.STORY),
                        new DrawCardEffect(1)
                ),
                "{W}, {T}, Remove a story counter from this artifact: Draw a card."
        ));
    }
}
