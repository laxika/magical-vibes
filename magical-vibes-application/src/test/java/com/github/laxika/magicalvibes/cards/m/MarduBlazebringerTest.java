package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class MarduBlazebringerTest extends BaseCardTest {

    @Test
    @DisplayName("Mardu Blazebringer is sacrificed at end of combat after attacking")
    void sacrificedAtEndOfCombatWhenAttacking() {
        Permanent blazebringer = new Permanent(new MarduBlazebringer());
        blazebringer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(blazebringer);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mardu Blazebringer");
        harness.assertInGraveyard(player1, "Mardu Blazebringer");
    }

    @Test
    @DisplayName("Mardu Blazebringer is sacrificed at end of combat after blocking")
    void sacrificedAtEndOfCombatWhenBlocking() {
        Permanent blazebringer = new Permanent(new MarduBlazebringer());
        blazebringer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blazebringer);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mardu Blazebringer");
        harness.assertInGraveyard(player2, "Mardu Blazebringer");
    }
}
