package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeviathanTest extends BaseCardTest {

    // ===== Upkeep: sacrifice two Islands to untap =====

    @Test
    @DisplayName("Upkeep: sacrificing two Islands untaps Leviathan")
    void upkeepSacrificeTwoIslandsUntaps() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new Leviathan());
        leviathan.tap();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        resolveUpkeepMay(player1, true);

        assertThat(leviathan.isTapped()).isFalse();
        assertThat(islandCount(player1)).isZero();
    }

    @Test
    @DisplayName("Upkeep: declining keeps Leviathan tapped and the Islands")
    void upkeepDeclineKeepsEverything() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new Leviathan());
        leviathan.tap();
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());

        resolveUpkeepMay(player1, false);

        assertThat(leviathan.isTapped()).isTrue();
        assertThat(islandCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep: accepting with only one Island sacrifices nothing and stays tapped")
    void upkeepAcceptWithOneIslandDoesNothing() {
        Permanent leviathan = harness.addToBattlefieldAndReturn(player1, new Leviathan());
        leviathan.tap();
        harness.addToBattlefield(player1, new Island());

        resolveUpkeepMay(player1, true);

        assertThat(leviathan.isTapped()).isTrue();
        assertThat(islandCount(player1)).isEqualTo(1);
    }

    // ===== Attack: can't attack unless you sacrifice two Islands =====

    @Test
    @DisplayName("Cannot attack when controlling fewer than two Islands")
    void cannotAttackWithoutTwoIslands() {
        addCreatureReady(player1, new Leviathan());
        harness.addToBattlefield(player1, new Island());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attacking sacrifices two Islands")
    void attackingSacrificesTwoIslands() {
        addCreatureReady(player1, new Leviathan());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player1, List.of(0));

        assertThat(islandCount(player1)).isZero();
        // Attack still went through (10/10 deals combat damage to the defender)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(lifeBefore);
    }

    // ===== Helpers =====

    private void resolveUpkeepMay(Player player, boolean accept) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
        harness.handleMayAbilityChosen(player, accept);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private int islandCount(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .count();
    }
}
