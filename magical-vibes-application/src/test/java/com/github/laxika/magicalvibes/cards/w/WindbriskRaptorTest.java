package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindbriskRaptorTest extends BaseCardTest {

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(bears);
        return bears;
    }

    @Test
    @DisplayName("Attacking creature you control gains its controller life via granted lifelink")
    void grantsLifelinkToOwnAttacker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new WindbriskRaptor()));
        addAttacker(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1));

        // Grizzly Bears (no innate lifelink) deals 2 combat damage; lifelink gains player1 2 life.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Without the Raptor, the same attacker gains no life (lifelink comes from the Raptor)")
    void noLifelinkWithoutRaptor() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        addAttacker(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // player2 loses 2; player1 gains nothing since the vanilla Grizzly Bears has no lifelink.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
