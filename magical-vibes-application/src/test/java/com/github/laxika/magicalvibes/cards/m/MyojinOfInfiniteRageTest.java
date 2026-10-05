package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ConsumingVortex;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyojinOfInfiniteRage.class, Mountain.class, LanternKami.class, ConsumingVortex.class})
class MyojinOfInfiniteRageTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters with a divinity counter and indestructible")
    void castFromHandEntersWithDivinityCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new MyojinOfInfiniteRage(), "{7}{R}{R}{R}");
        harness.passBothPriorities();

        Permanent myojin = findPermanent(player1, "Myojin of Infinite Rage");
        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast from hand does not get a divinity counter")
    void enteringWithoutCastingDoesNotGetDivinityCounter() {
        Permanent myojin = harness.enterBattlefieldAndReturn(player1, new MyojinOfInfiniteRage());

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Removing the divinity counter destroys all lands but no other permanents")
    void removingDivinityCounterDestroysAllLands() {
        Permanent myojin = addMyojinWithDivinityCounter(player1);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent creature = addCreatureReady(player2, new LanternKami());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(myojin).doesNotContain(ownLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature).doesNotContain(opposingLand);
    }

    @Test
    @DisplayName("Removing one of multiple divinity counters leaves indestructible active")
    void removingOneOfMultipleDivinityCountersLeavesIndestructible() {
        Permanent myojin = addMyojinWithDivinityCounter(player1);
        myojin.setCounterCount(CounterType.DIVINITY, 2);
        harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
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
    @DisplayName("The counter is paid immediately, before any lands are destroyed")
    void counterIsRemovedAsActivationCost() {
        Permanent myojin = harness.enterBattlefieldAndReturn(player1, new MyojinOfInfiniteRage());
        myojin.setCounterCount(CounterType.DIVINITY, 1);
        myojin.setTapped(true);
        harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertOnBattlefield(player2, "Mountain");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mountain");
        harness.assertNotOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("The land destruction resolves even if Myojin is returned to hand in response")
    void destroysLandsAfterSourceLeavesBattlefield() {
        Permanent myojin = addMyojinWithDivinityCounter(player1);
        harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ConsumingVortex()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castAndResolveInstant(player2, 0, myojin.getId());

        harness.assertInHand(player1, "Myojin of Infinite Rage");
        harness.assertNotOnBattlefield(player1, "Myojin of Infinite Rage");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Mountain");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    private Permanent addMyojinWithDivinityCounter(Player player) {
        Permanent myojin = addCreatureReady(player, new MyojinOfInfiniteRage());
        myojin.setCounterCount(CounterType.DIVINITY, 1);
        return myojin;
    }
}
