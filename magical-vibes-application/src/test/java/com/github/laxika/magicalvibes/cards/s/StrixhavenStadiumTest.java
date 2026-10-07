package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrixhavenStadium.class, EagerFirstYear.class})
class StrixhavenStadiumTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds a colorless mana and a point counter")
    void tappingAddsManaAndPointCounter() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(stadium.getCounterCount(CounterType.POINT)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to its controller removes a point counter")
    void combatDamageToControllerRemovesPointCounter() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());
        stadium.setCounterCount(CounterType.POINT, 1);
        Permanent attacker = addCreatureReady(player2, new EagerFirstYear());
        attacker.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(stadium.getCounterCount(CounterType.POINT)).isZero();
    }

    @Test
    @DisplayName("Combat damage to an opponent adds a point counter")
    void combatDamageToOpponentAddsPointCounter() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());
        Permanent attacker = addCreatureReady(player1, new EagerFirstYear());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(stadium.getCounterCount(CounterType.POINT)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ten point counters make the damaged opponent lose")
    void tenPointCountersMakeDamagedOpponentLose() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());
        stadium.setCounterCount(CounterType.POINT, 9);
        Permanent attacker = addCreatureReady(player1, new EagerFirstYear());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(stadium.getCounterCount(CounterType.POINT)).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The mana ability resolves immediately and reaching ten counters does not cause a loss")
    void tappingForTenthCounterDoesNotCauseLoss() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());

        stadium.setCounterCount(CounterType.POINT, 9);
        harness.activateAbility(player1, 0, null, null);

        assertThat(stadium.isTapped()).isTrue();
        assertThat(stadium.getCounterCount(CounterType.POINT)).isEqualTo(10);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Each creature dealing combat damage adds one counter regardless of damage amount")
    void multipleCreaturesAddOneCounterEach() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());

        addCreatureReady(player1, new EagerFirstYear()).setAttacking(true);
        addCreatureReady(player1, new EagerFirstYear()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(stadium.getCounterCount(CounterType.POINT)).isEqualTo(2);
        harness.assertLife(player2, 16);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Each creature dealing combat damage to the controller removes one counter")
    void multipleCreaturesRemoveOneCounterEach() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());

        stadium.setCounterCount(CounterType.POINT, 5);
        addCreatureReady(player2, new EagerFirstYear()).setAttacking(true);
        addCreatureReady(player2, new EagerFirstYear()).setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(stadium.getCounterCount(CounterType.POINT)).isEqualTo(3);
        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Combat damage to the controller with no counters has no counter effect")
    void incomingCombatDamageWithNoCounters() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());

        addCreatureReady(player2, new EagerFirstYear()).setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(stadium.getCounterCount(CounterType.POINT)).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("All point counters are removed above ten while other counters remain")
    void moreThanTenPointCountersCauseLoss() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());

        stadium.setCounterCount(CounterType.POINT, 12);
        stadium.setCounterCount(CounterType.CHARGE, 2);
        addCreatureReady(player1, new EagerFirstYear()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(stadium.getCounterCount(CounterType.POINT)).isZero();
        assertThat(stadium.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The threshold uses counters added in response to the combat trigger")
    void thresholdIsCheckedAtResolution() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());

        stadium.setCounterCount(CounterType.POINT, 8);
        addCreatureReady(player1, new EagerFirstYear()).setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        assertThat(stadium.getCounterCount(CounterType.POINT)).isEqualTo(8);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(stadium.getCounterCount(CounterType.POINT)).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A departing Stadium with ten counters still makes the damaged opponent lose")
    void departedStadiumUsesLastKnownCounters() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());

        stadium.setCounterCount(CounterType.POINT, 10);
        addCreatureReady(player1, new EagerFirstYear()).setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().removePermanentToHand(gd, stadium);
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A departing Stadium with nine counters cannot receive the tenth counter")
    void departedStadiumBelowThresholdDoesNotCauseLoss() {
        Permanent stadium = harness.addToBattlefieldAndReturn(player1, new StrixhavenStadium());

        stadium.setCounterCount(CounterType.POINT, 9);
        addCreatureReady(player1, new EagerFirstYear()).setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().removePermanentToHand(gd, stadium);
        resolveAllTriggers();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertLife(player2, 18);
    }
}
