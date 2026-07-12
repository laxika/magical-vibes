package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

class UrzasArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 of a noncombat damage source to the controller")
    void preventsOneNoncombatDamage() {
        harness.addToBattlefield(player1, new UrzasArmor());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Shock deals 2; 1 is prevented, so player1 takes 1.
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Two copies each prevent 1 (stacking)")
    void twoCopiesStack() {
        harness.addToBattlefield(player1, new UrzasArmor());
        harness.addToBattlefield(player1, new UrzasArmor());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        // Shock deals 2; both copies prevent 1 each, so all of it is prevented.
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only the controller is protected, not their opponent")
    void opponentDamageIsNotPrevented() {
        harness.addToBattlefield(player1, new UrzasArmor());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // player2 controls no Urza's Armor, so the full 2 damage lands.
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents 1 of each attacker's combat damage to the controller")
    void preventsOneCombatDamage() {
        harness.addToBattlefield(player1, new UrzasArmor());
        harness.setLife(player1, 20);

        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.<BlockerAssignment>of());
        harness.passBothPriorities();

        // Hill Giant deals 3; 1 is prevented, so player1 takes 2.
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }
}
