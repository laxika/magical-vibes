package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Crawlspace.class, GiantCockroach.class, NarsetParterOfVeils.class})
class CrawlspaceTest extends BaseCardTest {

    @Test
    @DisplayName("No more than two creatures can attack its controller")
    void limitsAttacksAgainstController() {
        harness.addToBattlefield(player2, new Crawlspace());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 2 creatures can attack");
    }

    @Test
    @DisplayName("Two creatures can attack its controller")
    void allowsTwoAttacksAgainstController() {
        harness.addToBattlefield(player2, new Crawlspace());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Does not limit creatures attacking the controller's planeswalker")
    void doesNotLimitAttacksAgainstPlaneswalker() {
        harness.addToBattlefield(player2, new Crawlspace());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NarsetParterOfVeils());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());

        Map<Integer, UUID> targets = Map.of(
                0, planeswalker.getId(),
                1, planeswalker.getId(),
                2, planeswalker.getId());
        assertThatCode(() -> declareAttackersAtTargets(player1, List.of(0, 1, 2), targets))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Does not limit creatures controlled by its controller attacking an opponent")
    void doesNotLimitItsControllersAttacks() {
        harness.addToBattlefield(player2, new Crawlspace());
        addCreatureReady(player2, new GiantCockroach());
        addCreatureReady(player2, new GiantCockroach());
        addCreatureReady(player2, new GiantCockroach());

        assertThatCode(() -> declareAttackers(player2, List.of(1, 2, 3)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Two creatures can attack the controller while another attacks their planeswalker")
    void allowsMixedAttacksWithinPlayerLimit() {
        harness.addToBattlefield(player2, new Crawlspace());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NarsetParterOfVeils());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());

        assertThatCode(() -> declareAttackersAtTargets(player1, List.of(0, 1, 2),
                Map.of(0, player2.getId(), 1, player2.getId(), 2, planeswalker.getId())))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Attacking a planeswalker does not permit a third creature to attack the controller")
    void rejectsMixedAttacksExceedingPlayerLimit() {
        harness.addToBattlefield(player2, new Crawlspace());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NarsetParterOfVeils());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());

        assertThatThrownBy(() -> declareAttackersAtTargets(player1, List.of(0, 1, 2, 3),
                Map.of(0, player2.getId(), 1, player2.getId(), 2, player2.getId(),
                        3, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No more than 2 creatures can attack");
    }

    @Test
    @DisplayName("Multiple Crawlspaces still allow two attackers")
    void multipleCopiesDoNotReduceLimit() {
        harness.addToBattlefield(player2, new Crawlspace());
        harness.addToBattlefield(player2, new Crawlspace());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("The attack limit ends when Crawlspace leaves the battlefield")
    void removalEndsAttackLimit() {
        Permanent crawlspace = harness.addToBattlefieldAndReturn(player2, new Crawlspace());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());
        addCreatureReady(player1, new GiantCockroach());
        gd.playerBattlefields.get(player2.getId()).remove(crawlspace);
        gd.playerGraveyards.get(player2.getId()).add(crawlspace.getCard());

        assertThatCode(() -> declareAttackers(player1, List.of(0, 1, 2))).doesNotThrowAnyException();
    }

    private void declareAttackersAtTargets(Player player, List<Integer> attackerIndices,
                                           Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

}
