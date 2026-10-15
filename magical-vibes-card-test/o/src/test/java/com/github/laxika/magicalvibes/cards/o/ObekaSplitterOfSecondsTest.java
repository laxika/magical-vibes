package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArmageddonClock;
import com.github.laxika.magicalvibes.cards.p.PortRazer;
import com.github.laxika.magicalvibes.cards.q.QuickDraw;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObekaSplitterOfSeconds.class, ArmageddonClock.class, SuntailHawk.class,
        PortRazer.class, QuickDraw.class})
class ObekaSplitterOfSecondsTest extends BaseCardTest {

    @Test
    @DisplayName("Gets additional upkeep steps equal to combat damage dealt to a player")
    void getsAdditionalUpkeepStepsEqualToCombatDamage() {
        Permanent obeka = addCreatureReady(player1, new ObekaSplitterOfSeconds());
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new ArmageddonClock());
        obeka.setAttacking(true);

        resolveCombat();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get additional upkeep steps when no combat damage reaches a player")
    void noAdditionalUpkeepStepsWithoutCombatDamageToPlayer() {
        Permanent obeka = addCreatureReady(player1, new ObekaSplitterOfSeconds());
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new ArmageddonClock());
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        obeka.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isZero();
    }

    @Test
    @DisplayName("Menace prevents a single creature from blocking Obeka")
    void cannotBeBlockedByOnlyOneCreature() {
        Permanent obeka = addCreatureReady(player1, new ObekaSplitterOfSeconds());
        harness.addToBattlefield(player2, new SuntailHawk());
        obeka.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Additional upkeeps begin after end of combat and skip untap and draw")
    void additionalUpkeepsSkipUntapAndDraw() {
        Permanent obeka = addCreatureReady(player1, new ObekaSplitterOfSeconds());
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new ArmageddonClock());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ObekaSplitterOfSeconds()));
        obeka.setAttacking(true);
        obeka.tap();

        resolveCombat();
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        assertThat(clock.getCounterCount(CounterType.DOOM)).isZero();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(2);
        assertThat(obeka.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Uses the combat damage amount even when Obeka's power changes before resolution")
    void snapshotsCombatDamageAmount() {
        Permanent obeka = addCreatureReady(player1, new ObekaSplitterOfSeconds());
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new ArmageddonClock());
        obeka.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        obeka.setAttacking(true);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        obeka.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(clock.getCounterCount(CounterType.DOOM)).isEqualTo(4);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A combat created after Obeka's upkeeps occurs before those upkeeps")
    void laterCreatedCombatPrecedesAdditionalUpkeeps() {
        Permanent obeka = addCreatureReady(player1, new ObekaSplitterOfSeconds());
        Permanent razer = addCreatureReady(player1, new PortRazer());
        harness.setHand(player1, List.of(new QuickDraw()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, List.of(obeka.getId(), player2.getId()));
        obeka.setAttacking(true);
        razer.setAttacking(true);

        resolveCombat();
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        harness.withAutoStop(TurnStep.UPKEEP, () ->
                harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }
}
