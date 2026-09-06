package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmyAnts.class, Python.class, Quicksand.class})
class ArmyAntsTest extends BaseCardTest {

    @Test
    @DisplayName("With multiple lands, controller chooses which to sacrifice then destroys target")
    void choosesLandThenDestroysTarget() {
        Permanent ants = addReadyAnts(player1);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        harness.addToBattlefield(player1, new Quicksand());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Quicksand());

        harness.activateAbility(player1, 0, null, enemyLand.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, ownLand.getId());
        harness.passBothPriorities();

        assertThat(ants.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownLand);
        harness.assertNotOnBattlefield(player2, "Quicksand");
        harness.assertInGraveyard(player2, "Quicksand");
    }

    @Test
    @DisplayName("With one land, auto-sacrifices it and destroys the targeted land")
    void autoSacrificesOnlyLand() {
        Permanent ants = addReadyAnts(player1);
        harness.addToBattlefield(player1, new Quicksand());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Quicksand());

        harness.activateAbility(player1, 0, null, enemyLand.getId());
        harness.passBothPriorities();

        assertThat(ants.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Quicksand");
        harness.assertNotOnBattlefield(player2, "Quicksand");
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void requiresLandToSacrifice() {
        Permanent ants = addReadyAnts(player1);
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Quicksand());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ants.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Quicksand");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        Permanent ants = addReadyAnts(player1);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        Permanent python = harness.addToBattlefieldAndReturn(player2, new Python());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, python.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ants.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownLand);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new ArmyAnts());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Quicksand());
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ants.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownLand);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenAlreadyTapped() {
        Permanent ants = addReadyAnts(player1);
        ants.tap();
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Quicksand());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Quicksand());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyLand.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownLand);
    }

    @Test
    @DisplayName("Ability fizzles if the target land leaves before resolution")
    void abilityFizzlesIfTargetLandLeavesBeforeResolution() {
        addReadyAnts(player1);
        harness.addToBattlefield(player1, new Quicksand());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Quicksand());

        harness.activateAbility(player1, 0, null, enemyLand.getId());
        gd.playerBattlefields.get(player2.getId()).remove(enemyLand);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Quicksand");
    }

    private Permanent addReadyAnts(Player player) {
        Permanent perm = addCreatureReady(player, new ArmyAnts());
        prepareMainPhase(player);
        return perm;
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
