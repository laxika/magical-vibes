package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HowlingGolem.class, BalothGorger.class, HighGround.class})
class HowlingGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking causes each player to draw a card")
    void attackingDrawsForEachPlayer() {
        addCreatureReady(player1, new HowlingGolem());

        int p1Hand = gd.playerHands.get(player1.getId()).size();
        int p2Hand = gd.playerHands.get(player2.getId()).size();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1Hand + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2Hand + 1);
    }

    @Test
    @DisplayName("Blocking causes each player to draw a card")
    void blockingDrawsForEachPlayer() {
        Permanent attacker = addCreatureReady(player1, new BalothGorger());
        attacker.setAttacking(true);
        addCreatureReady(player2, new HowlingGolem());

        int p1Hand = gd.playerHands.get(player1.getId()).size();
        int p2Hand = gd.playerHands.get(player2.getId()).size();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1Hand + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2Hand + 1);
    }

    @Test
    @DisplayName("Does nothing when it neither attacks nor blocks")
    void noDrawWhenNotInCombat() {
        addCreatureReady(player1, new HowlingGolem());

        declareAttackers(player1, List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each attacking Golem triggers its own draw for each player")
    void multipleAttackersEachDrawForBothPlayers() {
        addCreatureReady(player1, new HowlingGolem());
        addCreatureReady(player1, new HowlingGolem());
        int p1Hand = gd.playerHands.get(player1.getId()).size();
        int p2Hand = gd.playerHands.get(player2.getId()).size();

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1Hand + 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2Hand + 2);
    }

    @Test
    @DisplayName("Attack trigger still draws after the Golem leaves the battlefield")
    void attackTriggerSurvivesSourceLeavingBattlefield() {
        Permanent golem = addCreatureReady(player1, new HowlingGolem());
        int p1Hand = gd.playerHands.get(player1.getId()).size();
        int p2Hand = gd.playerHands.get(player2.getId()).size();

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(golem);
        gd.playerGraveyards.get(player1.getId()).add(golem.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1Hand + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2Hand + 1);
    }

    @Test
    @DisplayName("Being blocked does not cause an additional draw")
    void becomingBlockedDoesNotTriggerAnotherDraw() {
        addCreatureReady(player1, new HowlingGolem());
        addCreatureReady(player2, new BalothGorger());
        int p1Hand = gd.playerHands.get(player1.getId()).size();
        int p2Hand = gd.playerHands.get(player2.getId()).size();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1Hand + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2Hand + 1);
    }

    @Test
    @DisplayName("Blocking two attackers triggers only one draw for each player")
    void blockingMultipleCreaturesDrawsOnlyOnce() {
        Permanent firstAttacker = addCreatureReady(player1, new BalothGorger());
        Permanent secondAttacker = addCreatureReady(player1, new BalothGorger());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        addCreatureReady(player2, new HowlingGolem());
        harness.addToBattlefield(player2, new HighGround());
        int p1Hand = gd.playerHands.get(player1.getId()).size();
        int p2Hand = gd.playerHands.get(player2.getId()).size();

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1Hand + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2Hand + 1);
    }
}
