package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeeprootWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Deeproot Warrior has ON_BECOMES_BLOCKED effect with BoostSelfEffect +1/+1")
    void hasOnBecomesBlockedEffect() {
        DeeprootWarrior card = new DeeprootWarrior();

        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED).getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getEffects(EffectSlot.ON_BECOMES_BLOCKED).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Deeproot Warrior becomes blocked, a triggered ability is pushed onto the stack")
    void becomesBlockedPushesTriggerOntoStack() {
        Permanent warriorPerm = addWarriorReady(player1);
        warriorPerm.setAttacking(true);

        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Deeproot Warrior")
                        && entry.getSourcePermanentId().equals(warriorPerm.getId()));
    }

    @Test
    @DisplayName("Resolving becomes-blocked trigger gives +1/+1 until end of turn")
    void becomesBlockedTriggerGivesPlusOnePlusOne() {
        Permanent warriorPerm = addWarriorReady(player1);
        warriorPerm.setAttacking(true);

        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(warriorPerm.getPowerModifier()).isEqualTo(1);
        assertThat(warriorPerm.getToughnessModifier()).isEqualTo(1);
        assertThat(warriorPerm.getEffectivePower()).isEqualTo(3);
        assertThat(warriorPerm.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Becomes-blocked trigger fires only once even with multiple blockers")
    void becomesBlockedFiresOnceWithMultipleBlockers() {
        Permanent warriorPerm = addWarriorReady(player1);
        warriorPerm.getCard().setPower(4);
        warriorPerm.getCard().setToughness(4);
        warriorPerm.setAttacking(true);

        addReadyBears(player2);
        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long warriorTriggerCount = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Deeproot Warrior"))
                .count();
        assertThat(warriorTriggerCount).isEqualTo(1);

        harness.passBothPriorities();

        // Only +1/+1, not +2/+2
        assertThat(warriorPerm.getPowerModifier()).isEqualTo(1);
        assertThat(warriorPerm.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("+1/+1 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent warriorPerm = addWarriorReady(player1);
        warriorPerm.getCard().setPower(4);
        warriorPerm.getCard().setToughness(4);
        warriorPerm.setAttacking(true);

        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(warriorPerm.getPowerModifier()).isEqualTo(1);
        assertThat(warriorPerm.getToughnessModifier()).isEqualTo(1);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(warriorPerm.getPowerModifier()).isEqualTo(0);
        assertThat(warriorPerm.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addWarriorReady(Player player) {
        Permanent perm = new Permanent(new DeeprootWarrior());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
