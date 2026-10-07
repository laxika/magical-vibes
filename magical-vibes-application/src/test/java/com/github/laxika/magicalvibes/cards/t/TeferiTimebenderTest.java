package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.cards.s.SkitteringSurveyor;
import com.github.laxika.magicalvibes.cards.s.SteelLeafChampion;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TeferiTimebender.class, SteelLeafChampion.class, SkitteringSurveyor.class, MoxAmber.class, Island.class})
class TeferiTimebenderTest extends BaseCardTest {

    @Test
    @DisplayName("+2 ability untaps target creature")
    void plusTwoUntapsTargetCreature() {
        Permanent teferi = addReadyTeferi(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SteelLeafChampion());
        creature.tap();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(7); // 5 + 2
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+2 ability untaps target artifact")
    void plusTwoUntapsTargetArtifact() {
        Permanent teferi = addReadyTeferi(player1);
        Permanent surveyor = harness.addToBattlefieldAndReturn(player1, new SkitteringSurveyor());
        surveyor.tap();
        surveyor.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, surveyor.getId());
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(7); // 5 + 2
        assertThat(surveyor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+2 ability can be activated without a target (up to zero)")
    void plusTwoCanActivateWithoutTarget() {
        Permanent teferi = addReadyTeferi(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(7); // 5 + 2
    }

    @Test
    @DisplayName("-3 ability gains 2 life and draws 2 cards")
    void minusThreeGainsLifeAndDrawsCards() {
        Permanent teferi = addReadyTeferi(player1);
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int handSizeBefore = harness.getGameData().playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(2); // 5 - 3
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
    }

    @Test
    @DisplayName("-9 ability grants controller an extra turn")
    void minusNineGrantsExtraTurn() {
        Permanent teferi = addReadyTeferi(player1);
        teferi.setCounterCount(CounterType.LOYALTY, 9);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Teferi should have 0 loyalty (9 - 9) and be in graveyard
        harness.assertNotOnBattlefield(player1, "Teferi, Timebender");
        // Controller should have an extra turn queued
        assertThat(gd.extraTurns).contains(player1.getId());
    }

    @Test
    @DisplayName("Cannot activate -9 when loyalty is only 5")
    void cannotActivateMinusNineWithInsufficientLoyalty() {
        addReadyTeferi(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("+2 untaps an opponent's noncreature artifact")
    void plusTwoUntapsOpponentsNoncreatureArtifact() {
        Permanent teferi = addReadyTeferi(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MoxAmber());
        artifact.tap();

        harness.activateAbility(player1, 0, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    @Test
    @DisplayName("+2 rejects a land that is neither an artifact nor a creature")
    void plusTwoRejectsLand() {
        Permanent teferi = addReadyTeferi(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        land.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(land.isTapped()).isTrue();
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("+2 rejects Teferi himself as a target")
    void plusTwoRejectsPlaneswalker() {
        Permanent teferi = addReadyTeferi(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, teferi.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-3 resolves after paying the last loyalty counters")
    void minusThreeResolvesAfterTeferiLeavesBattlefield() {
        Permanent teferi = addReadyTeferi(player1);
        teferi.setCounterCount(CounterType.LOYALTY, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Teferi, Timebender");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Cannot activate another loyalty ability in the same turn")
    void cannotActivateSecondLoyaltyAbility() {
        Permanent teferi = addReadyTeferi(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only one loyalty ability");

        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }

    private Permanent addReadyTeferi(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TeferiTimebender());
        perm.setCounterCount(CounterType.LOYALTY, 5);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
