package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SlinkingGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives -3/-0 until end of turn")
    void blockingGivesMinusThreePower() {
        Permanent giant = addReadyGiant(player2);
        addReadyAttacker(player1, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(giant.getId());

        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(-3);
        assertThat(giant.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);   // 4 base - 3
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Becoming blocked gives -3/-0 until end of turn")
    void becomingBlockedGivesMinusThreePower() {
        Permanent giant = addReadyGiant(player1);
        giant.setAttacking(true);
        addReadyBlocker(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(-3);
        assertThat(giant.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("-3/-0 modifier resets at end of turn")
    void modifierResetsAtEndOfTurn() {
        Permanent giant = addReadyGiant(player2);
        addReadyAttacker(player1, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(-3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(giant.getPowerModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
    }

    private Permanent addReadyGiant(Player player) {
        Permanent perm = new Permanent(new SlinkingGiant());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyAttacker(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBlocker(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
