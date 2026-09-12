package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DualNature.class, PygmyRazorback.class})
class DualNatureTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature entering creates a copy for its controller")
    void nontokenCreatureEnteringCreatesCopyForItsController() {
        harness.addToBattlefield(player1, new DualNature());
        Permanent creature = castPygmyRazorback(player2);

        assertThat(countPermanents(player2, "Pygmy Razorback")).isEqualTo(2);
        assertThat(findPermanents(player2, "Pygmy Razorback")).anyMatch(permanent ->
                permanent.getCard().isToken());
        assertThat(findPermanents(player1, "Pygmy Razorback")).noneMatch(permanent ->
                permanent.getCard().isToken());
        assertThat(creature.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("A nontoken creature leaving exiles all same-name tokens")
    void nontokenCreatureLeavingExilesSameNameTokens() {
        harness.addToBattlefield(player1, new DualNature());
        Permanent creature = castPygmyRazorback(player2);
        Permanent token = findPermanents(player2, "Pygmy Razorback").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
        assertThat(findPermanents(player2, "Pygmy Razorback")).isEmpty();
    }

    @Test
    @DisplayName("Leaving Dual Nature exiles its created tokens even when another player controls them")
    void leavingDualNatureExilesCreatedTokens() {
        Permanent dualNature = harness.addToBattlefieldAndReturn(player1, new DualNature());
        castPygmyRazorback(player2);
        Permanent token = findPermanents(player2, "Pygmy Razorback").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dualNature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
        assertThat(findPermanents(player1, "Dual Nature")).isEmpty();
    }

    @Test
    @DisplayName("A copy trigger resolves even if Dual Nature leaves first")
    void copyTriggerStillCreatesUnlinkedTokenAfterDualNatureLeaves() {
        Permanent dualNature = harness.addToBattlefieldAndReturn(player1, new DualNature());
        preparePlayer1MainPhase();
        harness.castFromHand(player1, new PygmyRazorback(), "{1}{G}");
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dualNature));
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Pygmy Razorback")).hasSize(2);
    }

    @Test
    @DisplayName("A nontoken creature leaving exiles same-name tokens controlled by any player")
    void nontokenCreatureLeavingExilesSameNameTokensAcrossControllers() {
        harness.addToBattlefield(player1, new DualNature());
        Permanent leavingCreature = castPygmyRazorback(player1);
        Permanent remainingCreature = castPygmyRazorback(player2);
        Permanent remainingToken = findPermanents(player2, "Pygmy Razorback").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(findPermanents(player1, "Pygmy Razorback")).hasSize(2);
        assertThat(findPermanents(player2, "Pygmy Razorback")).hasSize(2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, leavingCreature));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Pygmy Razorback")).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(remainingToken);
        assertThat(findPermanents(player2, "Pygmy Razorback")).containsExactly(remainingCreature);
    }

    @Test
    @DisplayName("A token leaving does not exile other tokens with the same name")
    void tokenLeavingDoesNotExileOtherSameNameTokens() {
        harness.addToBattlefield(player1, new DualNature());
        castPygmyRazorback(player1);
        castPygmyRazorback(player1);

        Permanent tokenLeaving = findPermanents(player1, "Pygmy Razorback").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        Permanent remainingToken = findPermanents(player1, "Pygmy Razorback").stream()
                .filter(permanent -> permanent.getCard().isToken() && !permanent.equals(tokenLeaving))
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, tokenLeaving));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Pygmy Razorback")).contains(remainingToken);
        assertThat(findPermanents(player1, "Pygmy Razorback")).hasSize(3);
    }

    private Permanent castPygmyRazorback(Player player) {
        prepareMainPhase(player);
        harness.castFromHand(player, new PygmyRazorback(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanents(player, "Pygmy Razorback").stream()
                .filter(permanent -> !permanent.getCard().isToken())
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
