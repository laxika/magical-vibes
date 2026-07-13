package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaltropsTest extends BaseCardTest {

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(attacker);
        return attacker;
    }

    @Test
    @DisplayName("Triggers once per attacking creature, on the Caltrops controller's side")
    void triggersOnAttack() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Caltrops()));
        Permanent attacker = addAttacker(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Deals 1 damage to an opponent's attacker")
    void damagesOpponentAttacker() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Caltrops()));
        Permanent attacker = addAttacker(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0));
        harness.getStackResolutionService().resolveTopOfStack(gd);

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to the controller's own attacker too")
    void damagesOwnAttacker() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Caltrops()));
        Permanent attacker = addAttacker(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Caltrops is at index 0, the attacking Grizzly Bears at index 1.
        gs.declareAttackers(gd, player1, List.of(1));
        harness.getStackResolutionService().resolveTopOfStack(gd);

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fires once per attacker, damaging each")
    void firesOncePerAttacker() {
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Caltrops()));
        Permanent attacker1 = addAttacker(player2);
        Permanent attacker2 = addAttacker(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0, 1));
        assertThat(gd.stack).hasSize(2);

        harness.getStackResolutionService().resolveTopOfStack(gd);
        harness.getStackResolutionService().resolveTopOfStack(gd);

        assertThat(attacker1.getMarkedDamage()).isEqualTo(1);
        assertThat(attacker2.getMarkedDamage()).isEqualTo(1);
    }
}
