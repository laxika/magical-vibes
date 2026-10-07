package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.cards.w.WitherbloomCampus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TenuredInkcaster.class, WitherbloomCampus.class, EagerFirstYear.class})
class TenuredInkcasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on target creature")
    void etbPutsCounterOnTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());

        harness.setHand(player1, List.of(new TenuredInkcaster()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target a noncreature permanent")
    void etbCannotTargetLand() {
        UUID landId = harness.addToBattlefieldAndReturn(player1, new WitherbloomCampus()).getId();

        harness.setHand(player1, List.of(new TenuredInkcaster()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, landId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking with a creature that has a +1/+1 counter drains each opponent")
    void counteredCreatureAttackingDrainsAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TenuredInkcaster());
        Permanent attacker = addCreatureReady(player1, new EagerFirstYear());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Attacking with a creature without a +1/+1 counter does not trigger")
    void creatureWithoutCounterDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new TenuredInkcaster());
        addCreatureReady(player1, new EagerFirstYear());

        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB can put a counter on an opponent's creature")
    void etbCanTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TenuredInkcaster());
        harness.setHand(player1, List.of(new TenuredInkcaster()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Inkcaster triggers for its own attack, once regardless of the number of counters")
    void ownAttackWithMultipleCountersDrainsOnce() {
        Permanent attacker = addCreatureReady(player1, new TenuredInkcaster());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        });
    }

    @Test
    @DisplayName("Each qualifying attacker produces a separate drain trigger")
    void eachCounteredAttackerTriggersSeparately() {
        addCreatureReady(player1, new TenuredInkcaster());
        Permanent first = addCreatureReady(player1, new EagerFirstYear());
        Permanent second = addCreatureReady(player1, new EagerFirstYear());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1, 2));
            resolveAllTriggers();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        });
    }

    @Test
    @DisplayName("An opponent's countered attacker does not trigger Inkcaster")
    void opponentsCounteredAttackerDoesNotTrigger() {
        addCreatureReady(player1, new TenuredInkcaster());
        Permanent attacker = addCreatureReady(player2, new EagerFirstYear());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        });
    }

    @Test
    @DisplayName("Removing the attacker's counters after triggering does not stop the drain")
    void removingCountersAfterAttackDoesNotStopTrigger() {
        Permanent attacker = addCreatureReady(player1, new TenuredInkcaster());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
            resolveAllTriggers();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        });
    }
}
