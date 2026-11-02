package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.LibraryTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryWatchTest {

    @Test
    void everyWatchReadsItsOwnTriggerAndZone() {
        assertThat(Stream.of(LibraryWatch.values(), TargetWatch.values(), CounterWatch.values(),
                        DamageWatch.values(), CombatWatch.values(), SpellAbilityWatch.values())
                .flatMap(Arrays::stream)
                .map(watch -> Map.entry(watch.trigger(), watch.zone())))
                .doesNotHaveDuplicates();
    }

    @Test
    void everyLibraryWatchReadsAZoneItsTriggerFunctionsFrom() {
        assertThat(LibraryWatch.values()).allSatisfy(watch -> {
            boolean graveyardForm = watch.name().startsWith("GRAVEYARD_");
            boolean defaultZone = watch.trigger().defaultZones().functionsIn(watch.zone());
            assertThat(graveyardForm || defaultZone).as(watch.name()).isTrue();
            assertThat(watch.trigger()).isInstanceOf(LibraryTrigger.class);
        });
    }

    @Test
    void aWatchReadsOnlyTheAbilitiesOfItsTriggerFunctioningFromItsZone() {
        Card card = new Card();
        DrawCardEffect onBattlefield = new DrawCardEffect(1);
        GainLifeEffect fromGraveyard = new GainLifeEffect(2);
        DrawCardEffect everyDraw = new DrawCardEffect(3);
        card.addTrigger(Triggers.youDrawSecondCardEachTurn(), onBattlefield);
        card.addTrigger(new TriggerCondition(Triggers.youDrawSecondCardEachTurn(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, fromGraveyard);
        card.addTrigger(Triggers.youDrawCard(), everyDraw);

        assertThat(LibraryWatch.ON_CONTROLLER_DRAWS_SECOND_CARD.effectsOf(card)).containsExactly(onBattlefield);
        assertThat(LibraryWatch.GRAVEYARD_ON_CONTROLLER_DRAWS_SECOND_CARD.effectsOf(card))
                .containsExactly(fromGraveyard);
        assertThat(LibraryWatch.ON_CONTROLLER_DRAWS.effectsOf(card)).containsExactly(everyDraw);
        assertThat(LibraryWatch.ON_OPPONENT_DRAWS.effectsOf(card)).isEmpty();
    }

    @Test
    void theSelfDiscardWatchesReadTheCardWhereverItIs() {
        Card card = new Card();
        DrawCardEffect cycled = new DrawCardEffect(1);
        GainLifeEffect discardedByOpponent = new GainLifeEffect(2);
        card.addTrigger(Triggers.youCycleThisCard(), cycled);
        card.addTrigger(Triggers.opponentCausesYouToDiscardThisCard(), discardedByOpponent);

        assertThat(LibraryWatch.ON_SELF_CYCLED.zone()).isEqualTo(Zone.GRAVEYARD);
        assertThat(LibraryWatch.ON_SELF_CYCLED.effectsOf(card)).containsExactly(cycled);
        assertThat(LibraryWatch.ON_SELF_DISCARDED_BY_OPPONENT.effectsOf(card)).containsExactly(discardedByOpponent);
        assertThat(LibraryWatch.ON_SELF_DISCARDED.effectsOf(card)).isEmpty();
    }
}
