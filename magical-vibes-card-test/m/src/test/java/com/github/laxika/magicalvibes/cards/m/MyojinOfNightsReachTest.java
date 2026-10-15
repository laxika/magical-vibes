package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyojinOfNightsReach.class, DevotedRetainer.class, LanternKami.class})
class MyojinOfNightsReachTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters with a divinity counter and indestructible")
    void castFromHandEntersWithDivinityCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new MyojinOfNightsReach(), "{5}{B}{B}{B}");
        harness.passBothPriorities();

        Permanent myojin = findPermanent(player1, "Myojin of Night's Reach");
        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast from hand does not get a divinity counter")
    void enteringWithoutCastingDoesNotGetDivinityCounter() {
        Permanent myojin = harness.enterBattlefieldAndReturn(player1, new MyojinOfNightsReach());

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Removing the divinity counter makes each opponent discard their hand")
    void removingDivinityCounterEmptiesOpponentHand() {
        Permanent myojin = addReadyMyojin(player1);
        harness.setHand(player2, List.of(new DevotedRetainer(), new LanternKami()));
        harness.setHand(player1, List.of(new DevotedRetainer()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without a divinity counter")
    void cannotActivateWithoutDivinityCounter() {
        Permanent myojin = addReadyMyojin(player1);
        myojin.setCounterCount(CounterType.DIVINITY, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");
    }

    @Test
    @DisplayName("Removing the counter is an immediate cost, before opponents discard")
    void counterIsRemovedBeforeAbilityResolves() {
        Permanent myojin = addReadyMyojin(player1);
        harness.setHand(player2, List.of(new LanternKami()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Lantern Kami");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Myojin can activate its ability")
    void tappedSummoningSickMyojinCanActivate() {
        Permanent myojin = addReadyMyojin(player1);
        myojin.setSummoningSick(true);
        myojin.tap();
        harness.setHand(player2, List.of(new DevotedRetainer()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Devoted Retainer");
    }

    @Test
    @DisplayName("Removing one of two divinity counters preserves indestructible even with an empty opponent hand")
    void remainingDivinityCounterPreservesIndestructible() {
        Permanent myojin = addReadyMyojin(player1);
        myojin.setCounterCount(CounterType.DIVINITY, 2);
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private Permanent addReadyMyojin(Player player) {
        Permanent myojin = addCreatureReady(player, new MyojinOfNightsReach());
        myojin.setCounterCount(CounterType.DIVINITY, 1);
        return myojin;
    }
}
