package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "SLD", collectorNumber = "1863")
public class StaffOfTheStoryteller extends Card {

    public StaffOfTheStoryteller() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSpirit(1));
        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.STORY));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{W}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.STORY),
                        new DrawCardEffect()),
                "{W}, {T}, Remove a story counter from Staff of the Storyteller: Draw a card."
        ));
    }
}
