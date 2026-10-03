package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.s.Snapback;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClockworkHydra.class, AshcoatBear.class, Snapback.class})
class ClockworkHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithCounters() {
        Permanent hydra = castHydra();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Tapping puts a +1/+1 counter on it")
    void activatedAbilityAddsCounter() {
        Permanent hydra = addHydraWithCounters(player1, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hydra.isTapped()).isTrue();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking removes a counter and deals 1 damage to the chosen target")
    void attackingRemovesCounterAndDealsDamage() {
        Permanent hydra = addHydraWithCounters(player1, 4);
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        declareAttackers(player1, List.of(0));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking removes a counter and deals 1 damage to the chosen target")
    void blockingRemovesCounterAndDealsDamage() {
        Permanent attacker = addCreatureReady(player1, new AshcoatBear());
        attacker.setAttacking(true);
        Permanent hydra = addHydraWithCounters(player2, 4);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.handlePermanentChosen(player2, attacker.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing the last counter still deals damage before the Hydra dies")
    void lastCounterStillDealsDamage() {
        addHydraWithCounters(player1, 1);
        Permanent target = addCreatureReady(player2, new AshcoatBear());

        declareAttackers(player1, List.of(0));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Clockwork Hydra");
        harness.assertNotOnBattlefield(player1, "Clockwork Hydra");
    }

    @Test
    @DisplayName("An illegal damage target prevents counter removal")
    void illegalTargetPreservesCounter() {
        Permanent hydra = addHydraWithCounters(player1, 4);
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player2, List.of(new Snapback()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        declareAttackers(player1, List.of(0));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        harness.assertInHand(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("A Hydra returned to hand cannot remove a counter or deal damage")
    void sourceLeavingPreventsDamage() {
        Permanent hydra = addHydraWithCounters(player1, 4);
        Permanent target = addCreatureReady(player2, new AshcoatBear());
        harness.setHand(player2, List.of(new Snapback()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        declareAttackers(player1, List.of(0));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player2, 0, hydra.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInHand(player1, "Clockwork Hydra");
    }

    @Test
    @DisplayName("Attack damage can target a player and resolves with counter removal")
    void attackCanDamagePlayer() {
        Permanent hydra = addHydraWithCounters(player1, 4);
        addCreatureReady(player2, new AshcoatBear());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertLife(player2, 19);
    }

    private Permanent castHydra() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ClockworkHydra()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Clockwork Hydra");
    }

    private Permanent addHydraWithCounters(Player player, int counters) {
        Permanent hydra = addCreatureReady(player, new ClockworkHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return hydra;
    }
}
