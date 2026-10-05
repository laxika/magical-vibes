package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CloudcrestLake;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyojinOfCleansingFire.class, CloudcrestLake.class, LanternKami.class})
class MyojinOfCleansingFireTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters with a divinity counter and indestructible")
    void castFromHandEntersWithDivinityCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new MyojinOfCleansingFire(), "{5}{W}{W}{W}");
        harness.passBothPriorities();

        Permanent myojin = findPermanent(player1, "Myojin of Cleansing Fire");
        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast from hand does not get a divinity counter")
    void enteringWithoutCastingDoesNotGetDivinityCounter() {
        Permanent myojin = harness.enterBattlefieldAndReturn(player1, new MyojinOfCleansingFire());

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Removing the divinity counter destroys all other creatures")
    void removingDivinityCounterDestroysOtherCreatures() {
        Permanent myojin = addMyojinWithDivinityCounter(player1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LanternKami());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new CloudcrestLake());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new LanternKami());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(myojin, ownLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    @DisplayName("The ability cannot be activated without a divinity counter")
    void cannotActivateWithoutDivinityCounter() {
        Permanent myojin = addMyojinWithDivinityCounter(player1);
        myojin.setCounterCount(CounterType.DIVINITY, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("The divinity counter is paid immediately, before creatures are destroyed")
    void counterIsRemovedAsActivationCost() {
        Permanent myojin = addMyojinWithDivinityCounter(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(myojin);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Another Myojin without a divinity counter is destroyed")
    void otherMyojinIsNotExcludedByName() {
        Permanent source = addMyojinWithDivinityCounter(player1);
        Permanent other = harness.enterBattlefieldAndReturn(player2, new MyojinOfCleansingFire());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(other.getCard());
    }

    @Test
    @DisplayName("An opposing Myojin with a divinity counter survives the destruction")
    void otherIndestructibleCreatureSurvives() {
        Permanent source = addMyojinWithDivinityCounter(player1);
        Permanent other = addMyojinWithDivinityCounter(player2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(other).doesNotContain(creature);
        assertThat(other.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Myojin can activate on the opponent's turn")
    void canActivateTappedOnOpponentsTurn() {
        Permanent myojin = addMyojinWithDivinityCounter(player1);
        myojin.setTapped(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LanternKami());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(myojin);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(myojin.isTapped()).isTrue();
        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
    }

    private Permanent addMyojinWithDivinityCounter(Player player) {
        Permanent myojin = harness.addToBattlefieldAndReturn(player, new MyojinOfCleansingFire());
        myojin.setCounterCount(CounterType.DIVINITY, 1);
        return myojin;
    }
}
