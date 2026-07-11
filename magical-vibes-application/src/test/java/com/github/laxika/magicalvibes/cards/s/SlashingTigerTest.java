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

class SlashingTigerTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked creates one becomes-blocked trigger")
    void becomingBlockedCreatesTrigger() {
        Permanent tiger = addReadyTiger(player1);
        tiger.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getSourcePermanentId()).isEqualTo(tiger.getId());
    }

    @Test
    @DisplayName("When blocked Slashing Tiger gets +2/+2 until end of turn")
    void whenBlockedGetsPlusTwoPlusTwo() {
        Permanent tiger = addReadyTiger(player1);
        tiger.setAttacking(true);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(tiger.getPowerModifier()).isEqualTo(2);
        assertThat(tiger.getToughnessModifier()).isEqualTo(2);
        assertThat(tiger.getEffectivePower()).isEqualTo(5);
        assertThat(tiger.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Multiple blockers still grant only a single +2/+2 (not per blocker)")
    void multipleBlockersGrantSingleBoost() {
        Permanent tiger = addReadyTiger(player1);
        tiger.setAttacking(true);
        addReadyBears(player2);
        addReadyBears(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(tiger.getPowerModifier()).isEqualTo(2);
        assertThat(tiger.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("If unblocked no becomes-blocked trigger is created")
    void unblockedCreatesNoTrigger() {
        Permanent tiger = addReadyTiger(player1);
        tiger.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(tiger.getPowerModifier()).isZero();
        assertThat(tiger.getToughnessModifier()).isZero();
    }

    private Permanent addReadyTiger(Player player) {
        Permanent permanent = new Permanent(new SlashingTiger());
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
