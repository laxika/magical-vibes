package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodGluttonTest extends BaseCardTest {

    @Test
    void lifelinkGainsLifeOnCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bloodGlutton = new Permanent(new BloodGlutton());
        bloodGlutton.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bloodGlutton);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
