package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrumblingColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Crumbling Colossus deals its combat damage, then is sacrificed at end of combat")
    void sacrificedAtEndOfCombatAfterAttacking() {
        harness.setLife(player2, 20);

        Permanent colossus = new Permanent(new CrumblingColossus());
        colossus.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(colossus);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        harness.assertNotOnBattlefield(player1, "Crumbling Colossus");
        harness.assertInGraveyard(player1, "Crumbling Colossus");
    }

    @Test
    @DisplayName("Blocking with Crumbling Colossus does not trigger the sacrifice")
    void blockingDoesNotTriggerSacrifice() {
        Permanent colossus = new Permanent(new CrumblingColossus());
        colossus.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(colossus);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Crumbling Colossus");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
