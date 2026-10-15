package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeeprootWarrior.class})
class DeeprootWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("When Deeproot Warrior becomes blocked, a triggered ability is pushed onto the stack")
    void becomesBlockedPushesTriggerOntoStack() {
        Permanent warriorPerm = addWarriorReady(player1);
        warriorPerm.setAttacking(true);

        addCreatureReady(player2, new DeeprootWarrior());

        prepareDeclareBlockers();
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

        addCreatureReady(player2, new DeeprootWarrior());

        prepareDeclareBlockers();
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
        TestCards.mutableCard(warriorPerm).setPower(4);
        TestCards.mutableCard(warriorPerm).setToughness(4);
        warriorPerm.setAttacking(true);

        addCreatureReady(player2, new DeeprootWarrior());
        addCreatureReady(player2, new DeeprootWarrior());

        prepareDeclareBlockers();
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
        TestCards.mutableCard(warriorPerm).setPower(4);
        TestCards.mutableCard(warriorPerm).setToughness(4);
        warriorPerm.setAttacking(true);

        addCreatureReady(player2, new DeeprootWarrior());

        prepareDeclareBlockers();
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

    @Test
    @DisplayName("An unblocked attack does not grant a boost")
    void unblockedAttackDoesNotBoost() {
        Permanent warrior = addWarriorReady(player1);
        warrior.setAttacking(true);
        addWarriorReady(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(warrior.getPowerModifier()).isZero();
        assertThat(warrior.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking another creature does not grant the blocker a boost")
    void blockingDoesNotBoostBlocker() {
        Permanent attacker = addWarriorReady(player1);
        attacker.setAttacking(true);
        Permanent blocker = addWarriorReady(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    private Permanent addWarriorReady(Player player) {
        return addCreatureReady(player, new DeeprootWarrior());
    }
}
