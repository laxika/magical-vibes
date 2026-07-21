package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MayaelsAriaTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    /** A vanilla creature with the given power (toughness matched to power). */
    private Permanent addBeast(Player player, int power) {
        Card beast = new Card();
        beast.setName("Test Beast " + power);
        beast.setType(CardType.CREATURE);
        beast.setPower(power);
        beast.setToughness(power);
        return harness.addToBattlefieldAndReturn(player, beast);
    }

    // ===== Counter branch only: power 5–9 gets counters but no life / no win =====

    @Test
    @DisplayName("With a power-5 creature: a +1/+1 counter on each creature you control, no life gain, no win")
    void putsCountersWhenControllingPowerFiveCreature() {
        harness.addToBattlefield(player1, new MayaelsAria());
        Permanent beast = addBeast(player1, 5);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        int startingLife = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1); // no intervening-if — trigger always goes on the stack
        harness.passBothPriorities(); // resolve trigger

        // Each creature you control gets a counter (the power-5 creature satisfies the gate).
        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // The 5/5 becomes 6/6 — still below 10, so no life gained and the game continues.
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    // ===== Life branch reached via the counter placed earlier in the SAME resolution =====

    @Test
    @DisplayName("A power-9 creature is pushed to power 10 by its counter, then you gain 10 life")
    void gainsLifeWhenCounterPushesPowerToTen() {
        harness.addToBattlefield(player1, new MayaelsAria());
        Permanent beast = addBeast(player1, 9);
        int startingLife = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // 9 power + counter = 10 power, which meets the "power 10 or greater" check as it resolves.
        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 10);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED); // 10 < 20, no win
    }

    // ===== Win branch reached via the counter placed earlier in the SAME resolution =====

    @Test
    @DisplayName("A power-19 creature is pushed to power 20 by its counter, then you win the game")
    void winsWhenCounterPushesPowerToTwenty() {
        harness.addToBattlefield(player1, new MayaelsAria());
        addBeast(player1, 19);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        // 19 power + counter = 20 power, meeting the "power 20 or greater" win check as it resolves.
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gameLogContains("wins the game")).isTrue();
    }

    // ===== No qualifying creature: trigger resolves but does nothing =====

    @Test
    @DisplayName("With only a small creature: trigger resolves but places no counter, gains no life, no win")
    void doesNothingWithoutAPowerFiveCreature() {
        harness.addToBattlefield(player1, new MayaelsAria());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        int startingLife = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    // ===== Controller-only upkeep trigger =====

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new MayaelsAria());
        addBeast(player1, 20);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
