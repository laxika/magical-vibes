package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaywardGuideBeast.class, Forest.class, CanopyBaloth.class})
class WaywardGuideBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes its controller return a land they control")
    void combatDamageReturnsControllersLand() {
        Permanent attacker = addCreatureReady(player1, new WaywardGuideBeast());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new CanopyBaloth());
        harness.addToBattlefield(player2, new Forest());

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(ownLand.getId());

        harness.handlePermanentChosen(player1, ownLand.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Canopy Baloth");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Controller chooses exactly one of multiple lands to return")
    void choosesOneLand() {
        Permanent attacker = addCreatureReady(player1, new WaywardGuideBeast());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());
        harness.handlePermanentChosen(player1, secondLand.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(secondLand.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstLand).doesNotContain(secondLand);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Combat damage still happens when the controller has no land to return")
    void noLandsDoesNotReturnOpponentsLand() {
        Permanent attacker = addCreatureReady(player1, new WaywardGuideBeast());
        harness.addToBattlefield(player2, new Forest());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A controlled land owned by the opponent returns to the opponent's hand")
    void returnsLandToOwnerRatherThanController() {
        Permanent attacker = addCreatureReady(player1, new WaywardGuideBeast());
        Forest borrowedLand = new Forest();
        borrowedLand.setOwnerId(player2.getId());
        Permanent land = harness.addToBattlefieldAndReturn(player1, borrowedLand);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(borrowedLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("Combat damage only to a blocker does not return a land")
    void damageToBlockerDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new WaywardGuideBeast());
        Permanent blocker = addCreatureReady(player2, new CanopyBaloth());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(attacker.getId());

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
