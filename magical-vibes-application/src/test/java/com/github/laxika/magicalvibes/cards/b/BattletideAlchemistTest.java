package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InspiringCleric;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BattletideAlchemistTest extends BaseCardTest {

    // ===== Noncombat damage =====

    @Test
    @DisplayName("Prevents 1 damage from a source (X=1, only the Alchemist is a Cleric)")
    void preventsOneNoncombatDamage() {
        // Battletide Alchemist is itself a Cleric, so X = 1.
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Shock deals 2; 1 is prevented, so player1 takes 1.
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("X scales with the number of Clerics controlled")
    void preventionScalesWithClerics() {
        // Alchemist + Inspiring Cleric = 2 Clerics, so X = 2.
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.addToBattlefield(player1, new InspiringCleric());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Shock deals 2; all of it is prevented.
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only the Alchemist's controller is protected, not their opponent")
    void opponentDamageIsNotPrevented() {
        // player1 controls the Alchemist but Shocks player2, who controls no Cleric.
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // player2 has no Battletide Alchemist, so the full 2 damage lands.
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    // ===== Combat damage =====

    @Test
    @DisplayName("Prevents X of each attacker's combat damage to the controller")
    void preventsCombatDamagePerAttacker() {
        harness.addToBattlefield(player1, new BattletideAlchemist());
        harness.setLife(player1, 20);

        // player2 attacks player1 with a 3/3.
        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        // player1 controls a potential blocker (the Alchemist), so declare no blocks to reach combat damage.
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.<BlockerAssignment>of());
        harness.passBothPriorities();

        // Hill Giant deals 3; 1 (X=1) is prevented, so player1 takes 2.
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
