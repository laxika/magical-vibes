package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FolkOfAnHavvaTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives +2/+0 until end of turn")
    void blockTriggerGivesPlusTwoPlusZero() {
        Permanent folk = block();

        assertThat(folk.getPowerModifier()).isEqualTo(2);
        assertThat(folk.getToughnessModifier()).isEqualTo(0);
        assertThat(folk.getEffectivePower()).isEqualTo(3);
        assertThat(folk.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void modifierResetsAtEndOfTurn() {
        Permanent folk = block();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(folk.getPowerModifier()).isEqualTo(0);
        assertThat(folk.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost while it is not blocking")
    void noBoostWithoutBlocking() {
        Permanent folk = addFolkReady(player2);

        assertThat(folk.getPowerModifier()).isEqualTo(0);
        assertThat(folk.getEffectivePower()).isEqualTo(1);
    }

    private Permanent block() {
        Permanent folk = addFolkReady(player2);

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
        return folk;
    }

    private Permanent addFolkReady(Player player) {
        Permanent perm = new Permanent(new FolkOfAnHavva());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
