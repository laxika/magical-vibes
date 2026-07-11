package com.github.laxika.magicalvibes.cards.t;

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

class TrainedCheetahTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked creates a becomes-blocked trigger")
    void becomingBlockedCreatesTrigger() {
        Permanent cheetah = addReadyCheetah(player1);
        cheetah.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(cheetah.getId());
    }

    @Test
    @DisplayName("When blocked Trained Cheetah gets +1/+1 until end of turn")
    void blockedGivesPlusOnePlusOne() {
        Permanent cheetah = addReadyCheetah(player1);
        cheetah.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(cheetah.getPowerModifier()).isEqualTo(1);
        assertThat(cheetah.getToughnessModifier()).isEqualTo(1);
        assertThat(cheetah.getEffectivePower()).isEqualTo(3);
        assertThat(cheetah.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Blocked by two creatures still only gets +1/+1")
    void multipleBlockersStillOneBoost() {
        Permanent cheetah = addReadyCheetah(player1);
        cheetah.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(cheetah.getPowerModifier()).isEqualTo(1);
        assertThat(cheetah.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent cheetah = addReadyCheetah(player1);
        cheetah.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(cheetah.getPowerModifier()).isZero();
        assertThat(cheetah.getToughnessModifier()).isZero();
    }

    private Permanent addReadyCheetah(Player player) {
        Permanent permanent = new Permanent(new TrainedCheetah());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadyBears(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
