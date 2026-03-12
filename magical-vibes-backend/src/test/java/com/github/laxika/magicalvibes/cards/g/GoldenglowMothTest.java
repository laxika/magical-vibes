package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GoldenglowMoth;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoldenglowMothTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Goldenglow Moth has MayEffect wrapping GainLifeEffect on ON_BLOCK")
    void hasCorrectStructure() {
        GoldenglowMoth card = new GoldenglowMoth();

        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BLOCK).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_BLOCK).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) mayEffect.wrapped()).amount()).isEqualTo(4);
    }

    // ===== Blocking triggers may-gain-life and accepting gains life =====

    @Test
    @DisplayName("Blocking a creature and choosing yes gains 4 life")
    void blockingAndChoosingYesGainsLife() {
        addReadyMoth(player2);
        addReadyAttacker(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goldenglow Moth");

        // Resolve the trigger — should prompt for may ability
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        // Resolve the accepted triggered ability (GainLifeEffect now on the stack)
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }

    // ===== Blocking triggers may-gain-life and declining does not gain life =====

    @Test
    @DisplayName("Blocking a creature and choosing no does not gain life")
    void blockingAndChoosingNoDoesNotGainLife() {
        addReadyMoth(player2);
        addReadyAttacker(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the trigger — decline the may ability
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private Permanent addReadyMoth(Player player) {
        Permanent perm = new Permanent(new GoldenglowMoth());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyAttacker(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
