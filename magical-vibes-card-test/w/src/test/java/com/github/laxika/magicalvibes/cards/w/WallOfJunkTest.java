package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfJunk.class, GorillaWarrior.class, Rescind.class})
class WallOfJunkTest extends BaseCardTest {

    @Test
    @DisplayName("When Wall of Junk blocks, it returns to its owner's hand at end of combat")
    void blockingReturnsWallToOwnersHandAtEndOfCombat() {
        addCreatureReady(player1, new GorillaWarrior());
        addCreatureReady(player2, new WallOfJunk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.assertOnBattlefield(player2, "Wall of Junk");
        harness.assertNotInHand(player2, "Wall of Junk");

        resolveAllTriggers();
        harness.assertNotOnBattlefield(player2, "Wall of Junk");
        harness.assertInHand(player2, "Wall of Junk");
        harness.assertOnBattlefield(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Wall of Junk does not return when it never blocks")
    void doesNotReturnWhenItDoesNotBlock() {
        addCreatureReady(player1, new GorillaWarrior());
        addCreatureReady(player2, new WallOfJunk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertOnBattlefield(player2, "Wall of Junk");
        harness.assertNotInHand(player2, "Wall of Junk");
    }

    @Test
    @DisplayName("A Wall of Junk that leaves the battlefield before end of combat is not returned")
    void doesNotReturnIfItLeavesBeforeEndOfCombat() {
        addCreatureReady(player1, new GorillaWarrior());
        Permanent wall = addCreatureReady(player2, new WallOfJunk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(wall.getId()));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        harness.assertNotInHand(player2, "Wall of Junk");
    }

    @Test
    @DisplayName("The delayed return can be responded to, and does not return a new Wall permanent")
    void delayedReturnDoesNotFollowWallThatLeftAndReturned() {
        addCreatureReady(player1, new GorillaWarrior());
        Permanent wall = addCreatureReady(player2, new WallOfJunk());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.assertOnBattlefield(player2, "Wall of Junk");
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Rescind()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, wall.getId());
        harness.assertInHand(player2, "Wall of Junk");

        gd.playerHands.get(player2.getId()).remove(wall.getCard());
        Permanent returnedWall = harness.enterBattlefieldAndReturn(player2, wall.getCard());
        assertThat(returnedWall.getId()).isNotEqualTo(wall.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Wall of Junk");
        harness.assertNotInHand(player2, "Wall of Junk");
    }

    @Test
    @DisplayName("Changing the Wall's controller does not change the delayed trigger's controller")
    void delayedReturnRetainsOriginalAbilityController() {
        addCreatureReady(player1, new GorillaWarrior());
        Permanent wall = addCreatureReady(player2, new WallOfJunk());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        gd.playerBattlefields.get(player2.getId()).remove(wall);
        gd.playerBattlefields.get(player1.getId()).add(wall);
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getControllerId()).isEqualTo(player2.getId());
    }
}
