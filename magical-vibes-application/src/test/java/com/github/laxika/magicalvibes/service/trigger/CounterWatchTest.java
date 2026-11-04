package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CounterWatchTest {

    @Test
    void everyWatchReadsItsOwnTriggerAndZone() {
        assertThat(Stream.of(CounterWatch.values(), DamageWatch.values(), CombatWatch.values(),
                        SpellAbilityWatch.values())
                .flatMap(Arrays::stream)
                .map(watch -> Map.entry(watch.trigger(), watch.zone())))
                .doesNotHaveDuplicates();
    }

    @Test
    void aWatchReadsOnlyTheAbilitiesFunctioningFromItsZone() {
        Card card = new Card();
        DrawCardEffect fromBattlefield = new DrawCardEffect(1);
        DrawCardEffect fromGraveyard = new DrawCardEffect(2);
        card.addTrigger(Triggers.youProliferate(), fromBattlefield);
        card.addTrigger(new TriggerCondition(Triggers.youProliferate(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, fromGraveyard);

        assertThat(CounterAbilityReader.printedEffects(card, CounterWatch.ON_CONTROLLER_PROLIFERATES)).containsExactly(fromBattlefield);
        assertThat(CounterAbilityReader.printedEffects(card,
                CounterWatch.GRAVEYARD_ON_CONTROLLER_PROLIFERATES)).containsExactly(fromGraveyard);
        assertThat(CounterWatch.GRAVEYARD_ON_CONTROLLER_PROLIFERATES.zone()).isEqualTo(Zone.GRAVEYARD);
        assertThat(CounterAbilityReader.printedEffects(card, CounterWatch.ON_CONTROLLER_GETS_ENERGY)).isEmpty();
    }

    @Test
    void theExiledTimeCounterWatchReadsTheExiledCard() {
        Card card = new Card();
        DrawCardEffect draw = new DrawCardEffect(1);
        card.addTrigger(Triggers.timeCounterRemovedFromThisInExile(), draw);

        assertThat(CounterWatch.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE.zone()).isEqualTo(Zone.EXILE);
        assertThat(CounterAbilityReader.printedEffects(card,
                CounterWatch.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE)).containsExactly(draw);
        assertThat(CounterAbilityReader.printedEffects(card, CounterWatch.ON_SELF_TIME_COUNTERS_REMOVED)).isEmpty();
    }
}
