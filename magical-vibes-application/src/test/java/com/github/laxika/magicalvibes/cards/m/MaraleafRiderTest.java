package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GildedGoose;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({MaraleafRider.class, GildedGoose.class, GrizzlyBears.class, Forest.class})
class MaraleafRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Food makes the target creature block Maraleaf Rider this turn if able")
    void sacrificesFoodToForceTargetToBlock() {
        Permanent rider = addCreatureReady(player1, new MaraleafRider());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        createFood();

        harness.activateAbility(player1, indexOf(player1, rider), null, blocker.getId());

        assertThat(countPermanents(player1, "Food")).isZero();
        harness.passBothPriorities();

        rider.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("The target creature can satisfy Maraleaf Rider's requirement by blocking it")
    void targetCanBlockMaraleafRider() {
        Permanent rider = addCreatureReady(player1, new MaraleafRider());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        createFood();

        harness.activateAbility(player1, indexOf(player1, rider), null, blocker.getId());
        harness.passBothPriorities();

        rider.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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

    private void createFood() {
        harness.setHand(player1, List.of(new GildedGoose()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
