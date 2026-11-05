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
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

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

        assertThat(LibraryAbilityReader.readPrinted(card, LibraryWatch.ON_CONTROLLER_DRAWS))
                .extracting(LibraryAbilityReader.LibraryEffect::effect, LibraryAbilityReader.LibraryEffect::counts)
                .containsExactly(tuple(onBattlefield, Set.of(2)), tuple(everyDraw, Set.of()));
        assertThat(LibraryAbilityReader.readPrinted(card, LibraryWatch.GRAVEYARD_ON_CONTROLLER_DRAWS))
                .extracting(LibraryAbilityReader.LibraryEffect::effect, LibraryAbilityReader.LibraryEffect::counts)
                .containsExactly(tuple(fromGraveyard, Set.of(2)));
        assertThat(LibraryAbilityReader.printed(card, LibraryWatch.ON_OPPONENT_DRAWS)).isEmpty();
    }

    @Test
    void theSelfDiscardWatchesReadTheCardWhereverItIs() {
        Card card = new Card();
        DrawCardEffect cycled = new DrawCardEffect(1);
        GainLifeEffect discardedByOpponent = new GainLifeEffect(2);
        card.addTrigger(Triggers.youCycleThisCard(), cycled);
        card.addTrigger(Triggers.opponentCausesYouToDiscardThisCard(), discardedByOpponent);

        assertThat(LibraryWatch.ON_SELF_CYCLED.zone()).isEqualTo(Zone.GRAVEYARD);
        assertThat(LibraryAbilityReader.printedEffects(card, LibraryWatch.ON_SELF_CYCLED)).containsExactly(cycled);
        assertThat(LibraryAbilityReader.printedEffects(card, LibraryWatch.ON_SELF_DISCARDED_BY_OPPONENT))
                .containsExactly(discardedByOpponent);
        assertThat(LibraryAbilityReader.printedEffects(card, LibraryWatch.ON_SELF_DISCARDED)).isEmpty();
    }
}
