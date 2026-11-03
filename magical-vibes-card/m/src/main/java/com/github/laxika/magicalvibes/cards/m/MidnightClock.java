package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleControllerHandAndGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ELD", collectorNumber = "54")
@CardRegistration(set = "ELD", collectorNumber = "346")
@CardRegistration(set = "SLD", collectorNumber = "2217")
@CardRegistration(set = "NCC", collectorNumber = "226")
@CardRegistration(set = "M3C", collectorNumber = "189")
@CardRegistration(set = "OTC", collectorNumber = "100")
@CardRegistration(set = "WOC", collectorNumber = "99")
@CardRegistration(set = "VOC", collectorNumber = "108")
@CardRegistration(set = "DRC", collectorNumber = "79")
public class MidnightClock extends Card {

    public MidnightClock() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new PutCountersOnSelfEffect(CounterType.HOUR)),
                "{2}{U}: Put an hour counter on this artifact."
        ));

        addTrigger(Triggers.upkeep(ActivePlayer.ANY),
                new PutCountersOnSelfEffect(CounterType.HOUR));

        addTrigger(Triggers.countersPutOnThis(), TriggerOptions.DEFAULT.withEventCondition(
                new SourceCounterThreshold(12, CounterType.HOUR)),
                SequenceEffect.of(
                        new ShuffleControllerHandAndGraveyardIntoLibraryEffect(),
                        new DrawCardEffect(7),
                        new ExileSelfEffect()));
    }
}
