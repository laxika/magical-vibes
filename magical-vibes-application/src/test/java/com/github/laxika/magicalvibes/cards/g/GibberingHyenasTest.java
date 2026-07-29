package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GibberingHyenasTest extends BaseCardTest {

    @Test
    @DisplayName("Gibbering Hyenas can block a nonblack creature")
    void canBlockNonBlackCreature() {
        Permanent hyenas = new Permanent(new GibberingHyenas());
        hyenas.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(hyenas);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(hyenas.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gibbering Hyenas cannot block a black creature")
    void cannotBlockBlackCreature() {
        Permanent hyenas = new Permanent(new GibberingHyenas());
        hyenas.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(hyenas);

        Permanent attacker = new Permanent(new BogWraith());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block");
    }

    @Test
    @DisplayName("Gibbering Hyenas can attack a black creature's controller unhindered")
    void canAttackFreely() {
        harness.setLife(player2, 20);

        Permanent hyenas = new Permanent(new GibberingHyenas());
        hyenas.setSummoningSick(false);
        hyenas.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(hyenas);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
