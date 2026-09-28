package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "40K", collectorNumber = "4")
@CardRegistration(set = "40K", collectorNumber = "176")
@CardRegistration(set = "40K", collectorNumber = "180")
@CardRegistration(set = "40K", collectorNumber = "321")
public class TheSwarmlord extends Card {

    public TheSwarmlord() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new Scaled(new CommanderCastsFromCommandZoneThisGame(), 2)));

        TriggeringPermanentConditionalEffect drawForCounterBearer =
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        new DrawCardEffect());
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, drawForCounterBearer);
        addEffect(EffectSlot.ON_DEATH, drawForCounterBearer);
    }
}
