package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.LifeTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LifeWatchTest {

    @Test
    void everyWatchReadsItsOwnTriggerAndZone() {
        assertThat(Stream.of(LifeWatch.values(), TapWatch.values(), LibraryWatch.values(), TargetWatch.values(),
                        CounterWatch.values(), DamageWatch.values(), CombatWatch.values(), SpellAbilityWatch.values())
                .flatMap(Arrays::stream)
                .map(watch -> Map.entry(watch.trigger(), watch.zone())))
                .doesNotHaveDuplicates();
    }

    @Test
    void everyLifeWatchReadsALifeTriggerInAZoneItFunctionsFrom() {
        assertThat(LifeWatch.values()).allSatisfy(watch -> {
            assertThat(watch.trigger()).as(watch.name()).isInstanceOf(LifeTrigger.class);
            assertThat(watch.zone()).as(watch.name())
                    .isEqualTo(watch.name().startsWith("GRAVEYARD_") ? Zone.GRAVEYARD : Zone.BATTLEFIELD);
        });
    }

    @Test
    void aWatchReadsOnlyTheAbilitiesOfItsTriggerAndZone() {
        Card card = new Card();
        DrawCardEffect gained = new DrawCardEffect(1);
        GainLifeEffect gainedFromGraveyard = new GainLifeEffect(2);
        DrawCardEffect opponentLost = new DrawCardEffect(3);
        card.addTrigger(Triggers.youGainLife(), gained);
        card.addTrigger(new TriggerCondition(Triggers.youGainLife(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                gainedFromGraveyard);
        card.addTrigger(Triggers.opponentLosesLife(), opponentLost);

        assertThat(LifeAbilityReader.printedEffects(card, LifeWatch.ON_CONTROLLER_GAINS_LIFE)).containsExactly(gained);
        assertThat(LifeAbilityReader.printedEffects(card, LifeWatch.GRAVEYARD_ON_CONTROLLER_GAINS_LIFE))
                .containsExactly(gainedFromGraveyard);
        assertThat(LifeAbilityReader.printedEffects(card, LifeWatch.ON_OPPONENT_LOSES_LIFE))
                .containsExactly(opponentLost);
        assertThat(LifeAbilityReader.printedEffects(card, LifeWatch.ON_CONTROLLER_LOSES_LIFE)).isEmpty();
        assertThat(LifeAbilityReader.printedEffects(card, LifeWatch.ON_OPPONENT_GAINS_LIFE)).isEmpty();
    }
}
