package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RakdosShredFreakTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent shredFreak = new Permanent(new RakdosShredFreak());
        shredFreak.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(shredFreak);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
