package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.cards.r.RiptideTurtle;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({EntrancingLyre.class, GrizzlyBears.class, HillGiant.class,
        NyxbornCourser.class, ReturnToNature.class, RiptideTurtle.class})
class EntrancingLyreTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 taps a target creature with power 2 or less")
    void tapsCreatureWithinPowerLimit() {
        Permanent lyre = harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, bears.getId());
        harness.passBothPriorities();

        assertThat(lyre.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than X")
    void rejectsCreatureAbovePowerLimit() {
        harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, giant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power X or less");
    }

    @Test
    @DisplayName("The target remains tapped while the Lyre remains tapped")
    void targetRemainsTappedWhileLyreRemainsTapped() {
        harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, bears.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The target untaps after the Lyre untaps")
    void targetUntapsAfterLyreUntaps() {
        Permanent lyre = harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, bears.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);
        advanceToNextTurn(player1);

        assertThat(lyre.isTapped()).isFalse();
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    void canChooseToKeepLyreTappedDuringOwnUntapStep() {
        Permanent lyre = harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, false);
        advanceToNextTurn(player1);

        assertThat(lyre.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void alreadyTappedCreatureIsStillPreventedFromUntapping() {
        harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void zeroXCanTapZeroPowerCreatureWithoutMana() {
        Permanent lyre = harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent turtle = harness.addToBattlefieldAndReturn(player2, new RiptideTurtle());

        harness.activateAbility(player1, 0, 0, turtle.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player1);

        assertThat(lyre.isTapped()).isTrue();
        assertThat(turtle.isTapped()).isTrue();
    }

    @Test
    void increasedPowerBeforeResolutionMakesTargetIllegal() {
        Permanent lyre = harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(lyre.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        target.tap();
        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void increasedPowerAfterResolutionDoesNotEndRestriction() {
        harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void untappingThenRetappingBeforeResolutionDoesNotCreateRestriction() {
        Permanent lyre = harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, target.getId());
        lyre.untap();
        lyre.tap();

        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        advanceToNextTurn(player1);

        assertThat(lyre.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void destroyingLyreBeforeResolutionStillTapsCreatureWithoutRestrictingUntap() {
        Permanent lyre = harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, target.getId());
        harness.setHand(player2, List.of(new ReturnToNature()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castModalInstant(player2, 0, 0, List.of(lyre.getId()));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Entrancing Lyre");

        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void destroyingLyreAfterResolutionReleasesCreatureAtNextUntap() {
        Permanent lyre = harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, target.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ReturnToNature()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castModalInstant(player1, 0, 0, List.of(lyre.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Entrancing Lyre");
        assertThat(target.isTapped()).isTrue();
        advanceToNextTurn(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotTargetNoncreatureArtifactEvenWithLargeX() {
        harness.addToBattlefieldAndReturn(player1, new EntrancingLyre());
        Permanent otherLyre = harness.addToBattlefieldAndReturn(player2, new EntrancingLyre());
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 10, otherLyre.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UPKEEP);
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
