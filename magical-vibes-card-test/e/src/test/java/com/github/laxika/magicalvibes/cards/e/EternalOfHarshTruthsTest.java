package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EternalOfHarshTruths.class, FrilledSandwalla.class})
class EternalOfHarshTruthsTest extends BaseCardTest {

    private void addAttacker() {
        Permanent atk = harness.addToBattlefieldAndReturn(player1, new EternalOfHarshTruths());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
    }

    private void addBlocker() {
        harness.addToBattlefield(player2, new FrilledSandwalla());
    }

    @Test
    @DisplayName("Attacking unblocked draws a card and does not afflict the defender")
    void unblockedDrawsCard() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new FrilledSandwalla())));
        harness.setLife(player2, 20);
        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        // Advance into declare-blockers (defender has no blockers), firing the "attacks and isn't
        // blocked" trigger, then resolve it.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Only the 1 unblocked combat damage lands (20 - 1); afflict does NOT fire when unblocked
        // (were it firing, the defender would be at 17: 20 - 1 combat - 2 afflict).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Afflict 2: becoming blocked makes the defending player lose 2 life")
    void blockedAfflictsDefender() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new FrilledSandwalla())));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addAttacker();
        addBlocker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Afflict is not a drain and does not fire the unblocked-attack draw.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Afflict triggers only once when two creatures block")
    void multipleBlockersLoseOnlyTwoLife() {
        harness.setHand(player1, List.of());
        harness.setLife(player2, 20);
        addAttacker();
        addBlocker();
        addBlocker();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Unblocked attack draws before combat damage even if the attacker leaves")
    void unblockedTriggerSurvivesSourceLeaving() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new FrilledSandwalla()));
        harness.setLife(player2, 20);
        addAttacker();
        addBlocker();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 20);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Frilled Sandwalla");
        harness.assertLife(player2, 20);
    }
}
