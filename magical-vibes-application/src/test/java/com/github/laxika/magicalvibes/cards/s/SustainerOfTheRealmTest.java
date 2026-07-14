package com.github.laxika.magicalvibes.cards.s;

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

class SustainerOfTheRealmTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking pushes a triggered ability onto the stack")
    void blockTriggerPushesOntoStack() {
        Permanent sustainer = addSustainerReady(player2);

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
        assertThat(entry.getSourcePermanentId()).isEqualTo(sustainer.getId());
    }

    @Test
    @DisplayName("Resolving the block trigger gives +0/+2 until end of turn")
    void blockTriggerGivesPlusZeroPlusTwo() {
        Permanent sustainer = addSustainerReady(player2);

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

        assertThat(sustainer.getPowerModifier()).isEqualTo(0);
        assertThat(sustainer.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("+0/+2 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent sustainer = addSustainerReady(player2);

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

        assertThat(sustainer.getToughnessModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sustainer.getPowerModifier()).isEqualTo(0);
        assertThat(sustainer.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("No trigger fires when the creature is not blocking")
    void noTriggerWhenNotBlocking() {
        addSustainerReady(player2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addSustainerReady(Player player) {
        Permanent perm = new Permanent(new SustainerOfTheRealm());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
