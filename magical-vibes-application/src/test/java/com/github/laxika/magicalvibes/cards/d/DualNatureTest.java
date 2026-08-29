package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DualNatureTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature entering creates a copy for its controller")
    void nontokenCreatureEnteringCreatesCopyForItsController() {
        harness.addToBattlefield(player1, new DualNature());
        Permanent creature = castGrizzlyBears(player2);

        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent ->
                permanent.getCard().isToken() && permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent ->
                permanent.getCard().isToken() && permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(creature.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("A nontoken creature leaving exiles all same-name tokens")
    void nontokenCreatureLeavingExilesSameNameTokens() {
        harness.addToBattlefield(player1, new DualNature());
        Permanent creature = castGrizzlyBears(player2);
        Permanent token = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Leaving Dual Nature exiles the tokens it created")
    void leavingDualNatureExilesCreatedTokens() {
        Permanent dualNature = harness.addToBattlefieldAndReturn(player1, new DualNature());
        castGrizzlyBears(player1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dualNature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(findPermanents(player1, "Dual Nature")).isEmpty();
    }

    @Test
    @DisplayName("A copy trigger resolves even if Dual Nature leaves first")
    void copyTriggerStillCreatesUnlinkedTokenAfterDualNatureLeaves() {
        Permanent dualNature = harness.addToBattlefieldAndReturn(player1, new DualNature());
        preparePlayer1MainPhase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dualNature));
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
    }

    private Permanent castGrizzlyBears(Player player) {
        prepareMainPhase(player);
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears")
                        && !permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void prepareMainPhase(Player player) {
        harness.forceActivePlayer(player);
        preparePlayer1MainPhaseIfNeeded(player);
        harness.clearPriorityPassed();
    }

    private void preparePlayer1MainPhase() {
        prepareMainPhase(player1);
    }

    private void preparePlayer1MainPhaseIfNeeded(Player player) {
        if (gd.currentStep != TurnStep.PRECOMBAT_MAIN || !gd.activePlayerId.equals(player.getId())) {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        }
    }
}
