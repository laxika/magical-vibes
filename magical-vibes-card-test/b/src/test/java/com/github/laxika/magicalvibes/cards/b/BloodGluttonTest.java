package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodGlutton.class})
class BloodGluttonTest extends BaseCardTest {

    @Test
    void lifelinkGainsLifeOnCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addCreatureReady(player1, new BloodGlutton());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void bothControllersGainFullDamageWhenAttackerAndBlockerDie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new BloodGlutton());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BloodGlutton());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 24);
        harness.assertNotOnBattlefield(player1, "Blood Glutton");
        harness.assertNotOnBattlefield(player2, "Blood Glutton");
        harness.assertInGraveyard(player1, "Blood Glutton");
        harness.assertInGraveyard(player2, "Blood Glutton");
    }
}
