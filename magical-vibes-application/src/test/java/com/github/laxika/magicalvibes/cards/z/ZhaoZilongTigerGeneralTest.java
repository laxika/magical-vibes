package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZhaoZilongTigerGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent zhao = addZhaoReady(player2);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(zhao.getId());
    }

    @Test
    @DisplayName("Resolving the block trigger gives +1/+1 until end of turn")
    void blockTriggerGivesPlusOnePlusOne() {
        Permanent zhao = addZhaoReady(player2);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(zhao.getPowerModifier()).isEqualTo(1);
        assertThat(zhao.getToughnessModifier()).isEqualTo(1);
        assertThat(zhao.getEffectivePower()).isEqualTo(4);
        assertThat(zhao.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("+1/+1 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent zhao = addZhaoReady(player2);

        Permanent atkPerm = new Permanent(new GrizzlyBears());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(zhao.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(zhao.getPowerModifier()).isEqualTo(0);
        assertThat(zhao.getToughnessModifier()).isEqualTo(0);
        assertThat(zhao.getEffectivePower()).isEqualTo(3);
        assertThat(zhao.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addZhaoReady(Player player) {
        Permanent perm = new Permanent(new ZhaoZilongTigerGeneral());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
