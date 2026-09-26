package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({MyojinOfSeeingWinds.class, LanternKami.class, Forest.class})
class MyojinOfSeeingWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters with a divinity counter and indestructible")
    void castFromHandEntersWithDivinityCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new MyojinOfSeeingWinds(), "{7}{U}{U}{U}");
        harness.passBothPriorities();

        Permanent myojin = findPermanent(player1, "Myojin of Seeing Winds");
        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast from hand does not get a divinity counter")
    void enteringWithoutCastingDoesNotGetDivinityCounter() {
        Permanent myojin = harness.enterBattlefieldAndReturn(player1, new MyojinOfSeeingWinds());

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Removing the divinity counter draws a card for each permanent you control")
    void removingDivinityCounterDrawsPerPermanent() {
        Permanent myojin = addReadyMyojin(player1);
        harness.addToBattlefield(player1, new LanternKami());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new LanternKami());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
    }

    @Test
    @DisplayName("Counts permanents controlled when the ability resolves")
    void countsPermanentsAtResolution() {
        addReadyMyojin(player1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new LanternKami());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Removing one divinity counter leaves indestructible while another remains")
    void removesOnlyOneDivinityCounter() {
        Permanent myojin = addReadyMyojin(player1);
        myojin.setCounterCount(CounterType.DIVINITY, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.DIVINITY)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
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

    private Permanent addReadyMyojin(Player player) {
        Permanent myojin = addCreatureReady(player, new MyojinOfSeeingWinds());
        myojin.setCounterCount(CounterType.DIVINITY, 1);
        return myojin;
    }
}
