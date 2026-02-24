package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenBlockingKeywordEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EzurisArchersTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Ezuri's Archers has BoostSelfWhenBlockingKeywordEffect for flying")
    void hasCorrectStructure() {
        EzurisArchers card = new EzurisArchers();

        assertThat(card.getEffects(EffectSlot.ON_BLOCK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BLOCK).getFirst())
                .isInstanceOf(BoostSelfWhenBlockingKeywordEffect.class);
        BoostSelfWhenBlockingKeywordEffect effect =
                (BoostSelfWhenBlockingKeywordEffect) card.getEffects(EffectSlot.ON_BLOCK).getFirst();
        assertThat(effect.requiredKeyword()).isEqualTo(Keyword.FLYING);
        assertThat(effect.powerBoost()).isEqualTo(3);
        assertThat(effect.toughnessBoost()).isEqualTo(0);
    }

    // ===== Blocking a flying creature triggers boost =====

    @Test
    @DisplayName("Blocking a creature with flying triggers +3/+0 boost")
    void blockingFlyingCreatureTriggersBoost() {
        Permanent archers = addReadyArchers(player2);
        Permanent flyer = addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Trigger should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ezuri's Archers");

        // Resolve the trigger
        harness.passBothPriorities();

        // Archers should have +3/+0
        assertThat(archers.getPowerModifier()).isEqualTo(3);
        assertThat(archers.getToughnessModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(4);   // 1 base + 3
        assertThat(gqs.getEffectiveToughness(gd, archers)).isEqualTo(2); // 2 base + 0
    }

    // ===== Blocking a non-flying creature does NOT trigger =====

    @Test
    @DisplayName("Blocking a creature without flying does not trigger boost")
    void blockingNonFlyingCreatureDoesNotTrigger() {
        Permanent archers = addReadyArchers(player2);
        addReadyAttacker(player1, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // No trigger should be on the stack
        assertThat(gd.stack).isEmpty();

        // Archers should have no boost
        assertThat(archers.getPowerModifier()).isEqualTo(0);
        assertThat(archers.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Boost is until end of turn (resets) =====

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        Permanent archers = addReadyArchers(player2);
        addReadyAttacker(player1, new SuntailHawk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(archers.getPowerModifier()).isEqualTo(3);

        // Advance to cleanup step (end of turn resets modifiers)
        harness.forceStep(TurnStep.CLEANUP);
        archers.resetModifiers();

        assertThat(archers.getPowerModifier()).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, archers)).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addReadyArchers(Player player) {
        Permanent perm = new Permanent(new EzurisArchers());
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
