package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoarQPine.class, LightningStrike.class})
class BoarQPineTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts a +1/+1 counter on Boar-q-pine")
    void noncreatureSpellAddsCounter() {
        harness.addToBattlefield(player1, new BoarQPine());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        Permanent boar = findBoar();
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(1);

        resolveAllTriggers();

        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Boar-q-pine")
    void creatureSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new BoarQPine());
        harness.setHand(player1, List.of(new BoarQPine()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent boar = findBoar();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not trigger Boar-q-pine")
    void opponentNoncreatureSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new BoarQPine());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);

        Permanent boar = findBoar();
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Multiple noncreature spells accumulate +1/+1 counters")
    void multipleNoncreatureSpellsAccumulateCounters() {
        harness.addToBattlefield(player1, new BoarQPine());
        harness.setHand(player1, List.of(new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 4);

        Permanent boar = findBoar();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The counter resolves before the spell, including during an opponent's turn")
    void counterResolvesBeforeSpellOnOpponentTurn() {
        harness.addToBattlefield(player1, new BoarQPine());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent boar = findBoar();

        harness.castInstant(player1, 0, player2.getId());

        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Each Boar-q-pine gets its own counter from a single noncreature spell")
    void multipleCopiesEachGetOneCounter() {
        harness.addToBattlefield(player1, new BoarQPine());
        Permanent first = findBoar();
        harness.addToBattlefield(player1, new BoarQPine());
        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A pending trigger cannot put counters on another copy after its source dies")
    void removedSourceDoesNotGiveCounterToOtherCopy() {
        harness.addToBattlefield(player1, new BoarQPine());
        Permanent first = findBoar();
        harness.addToBattlefield(player1, new BoarQPine());
        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.assertInGraveyard(player1, "Boar-q-pine");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    private Permanent findBoar() {
        return findPermanent(player1, "Boar-q-pine");
    }
}
