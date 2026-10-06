package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GildedGoose;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaraleafRider.class, GildedGoose.class, Forest.class})
class MaraleafRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Food makes the target creature block Maraleaf Rider this turn if able")
    void sacrificesFoodToForceTargetToBlock() {
        Permanent rider = addCreatureReady(player1, new MaraleafRider());
        Permanent blocker = addCreatureReady(player2, new MaraleafRider());
        createFood();

        harness.activateAbility(player1, indexOf(player1, rider), null, blocker.getId());

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.passBothPriorities();

        rider.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("The target creature can satisfy Maraleaf Rider's requirement by blocking it")
    void targetCanBlockMaraleafRider() {
        Permanent rider = addCreatureReady(player1, new MaraleafRider());
        Permanent blocker = addCreatureReady(player2, new MaraleafRider());
        createFood();

        harness.activateAbility(player1, indexOf(player1, rider), null, blocker.getId());
        harness.passBothPriorities();

        rider.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, rider))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent rider = addCreatureReady(player1, new MaraleafRider());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        createFood();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, rider), null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A non-Food permanent cannot pay the activation cost")
    void cannotActivateWithoutFood() {
        Permanent rider = addCreatureReady(player1, new MaraleafRider());
        Permanent blocker = addCreatureReady(player2, new MaraleafRider());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, rider), null, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped summoning-sick Rider may activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent rider = harness.addToBattlefieldAndReturn(player1, new MaraleafRider());
        rider.setSummoningSick(true);
        rider.tap();
        Permanent blocker = addCreatureReady(player2, new MaraleafRider());
        createFood();

        harness.activateAbility(player1, indexOf(player1, rider), null, blocker.getId());
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        rider.untap();
        rider.setSummoningSick(false);
        rider.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("A tapped target is unable to block and may be left unassigned")
    void tappedTargetDoesNotHaveToBlock() {
        Permanent rider = addCreatureReady(player1, new MaraleafRider());
        Permanent blocker = addCreatureReady(player2, new MaraleafRider());
        blocker.tap();
        createFood();

        harness.activateAbility(player1, indexOf(player1, rider), null, blocker.getId());
        harness.passBothPriorities();

        rider.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The target is not required to block a different attacker when Rider does not attack")
    void nonattackingSourceDoesNotForceOtherBlocks() {
        Permanent rider = addCreatureReady(player1, new MaraleafRider());
        Permanent otherAttacker = addCreatureReady(player1, new MaraleafRider());
        Permanent blocker = addCreatureReady(player2, new MaraleafRider());
        createFood();

        harness.activateAbility(player1, indexOf(player1, rider), null, blocker.getId());
        harness.passBothPriorities();

        otherAttacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The blocking requirement expires at the end of the turn")
    void blockingRequirementExpiresAtEndOfTurn() {
        Permanent rider = addCreatureReady(player1, new MaraleafRider());
        Permanent blocker = addCreatureReady(player2, new MaraleafRider());
        createFood();

        harness.activateAbility(player1, indexOf(player1, rider), null, blocker.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        rider.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        assertThat(blocker.isBlocking()).isFalse();
    }

    private void createFood() {
        harness.castFromHand(player1, new GildedGoose(), "{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
