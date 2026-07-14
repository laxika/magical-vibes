package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class BatwingBrumeTest extends BaseCardTest {

    @Test
    @DisplayName("{W} spent: prevents all combat damage this turn")
    void whiteSpentPreventsCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addAttacker(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BatwingBrume()));
        harness.addMana(player1, ManaColor.WHITE, 2); // {1}{W/B} both paid white → only {W} spent
        harness.castInstant(player1, 0);
        harness.passBothPriorities(); // resolve Batwing Brume, then combat damage (prevented)

        // Attacker's 2 damage to player2 is prevented; no black spent so no drain.
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("{B} spent: each player loses 1 life per attacking creature they control")
    void blackSpentDrainsPerAttacker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        // Two attackers for player1, one for player2 — the drain is per-controller.
        addAttacker(player1, new GrizzlyBears());
        addAttacker(player1, new GrizzlyBears());
        addAttacker(player2, new GrizzlyBears());

        // Cast in the postcombat main so no combat damage confounds the life totals; the drain
        // only reads each permanent's attacking flag.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BatwingBrume()));
        harness.addMana(player1, ManaColor.BLACK, 2); // only {B} spent
        harness.castInstant(player1, 0);
        harness.passBothPriorities(); // resolve Batwing Brume

        harness.assertLife(player1, 18); // two attackers
        harness.assertLife(player2, 19); // one attacker
    }

    @Test
    @DisplayName("{B} spent alone does not prevent combat damage")
    void blackSpentDoesNotPreventCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addAttacker(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BatwingBrume()));
        harness.addMana(player1, ManaColor.BLACK, 2); // only {B} spent → no prevention
        harness.castInstant(player1, 0);
        harness.passBothPriorities(); // resolve Batwing Brume, then combat damage (not prevented)

        // Drain: player1 loses 1 for its lone attacker; that attacker's 2 combat damage still lands.
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
    }

    // ===== Helpers =====

    private void addAttacker(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        perm.setAttacking(true);
    }
}
