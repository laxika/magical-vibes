package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NalathniDragon.class, Memnite.class})
class NalathniDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability gives +1/+0")
    void activatingAbilityBoostsPower() {
        Permanent dragon = addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(1);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate multiple times, with each activation adding +1/+0")
    void canActivateMultipleTimes() {
        Permanent dragon = addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(dragon.getPowerModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("No sacrifice at end step when activated fewer than four times")
    void noSacrificeWhenActivatedFewerThanFourTimes() {
        addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Nalathni Dragon");
    }

    @Test
    @DisplayName("Sacrificed at end step when activated four times")
    void sacrificedWhenActivatedFourTimes() {
        addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nalathni Dragon");
        harness.assertInGraveyard(player1, "Nalathni Dragon");
    }

    @Test
    @DisplayName("Activating four times during the end step waits for the next end step")
    void activationDuringEndStepWaitsForNextEndStep() {
        addCreatureReady(player1, new NalathniDragon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Nalathni Dragon");
        harness.assertInGraveyard(player1, "Nalathni Dragon");
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent dragon = addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dragon.getPowerModifier()).isEqualTo(0);
        assertThat(dragon.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Each stacked activation checks the activation count when it resolves")
    void stackedActivationsEachScheduleSacrifice() {
        Permanent dragon = addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
        }
        resolveAllTriggers();

        assertThat(dragon.getPowerModifier()).isEqualTo(4);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Nalathni Dragon");
    }

    @Test
    @DisplayName("Activation counts reset each turn")
    void activationCountResetsEachTurn() {
        addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 3);
        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Nalathni Dragon");
    }

    @Test
    @DisplayName("Different Dragons count their own activations separately")
    void activationCountsAreSeparateForEachDragon() {
        Permanent first = addCreatureReady(player1, new NalathniDragon());
        Permanent second = addCreatureReady(player1, new NalathniDragon());
        harness.addMana(player1, ManaColor.RED, 4);
        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, i % 2, null, null);
            harness.passBothPriorities();
        }

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block an unbanded Dragon")
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new NalathniDragon());
        addCreatureReady(player2, new Memnite());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Blocking a ground band member also blocks the Dragon and its controller divides blocker damage")
    void bandSharesBlocksAndControlsBlockerDamage() {
        Permanent dragon = addCreatureReady(player1, new NalathniDragon());
        Permanent bandmate = addCreatureReady(player1, new Memnite());
        Permanent blocker = addCreatureReady(player2, new Memnite());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.inMutationScope(() -> harness.getCombatAttackService()
                        .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)))));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        harness.handleCombatDamageAssigned(player1, 0, Map.of(bandmate.getId(), 1));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dragon).doesNotContain(bandmate);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A blocking Dragon lets its controller divide the attacker's damage")
    void bandingBlockerControlsAttackerDamage() {
        Permanent attacker = addCreatureReady(player1, new Memnite());
        Permanent dragon = addCreatureReady(player2, new NalathniDragon());
        Permanent otherBlocker = addCreatureReady(player2, new Memnite());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());
        harness.handleCombatDamageAssigned(player2, 0, Map.of(otherBlocker.getId(), 1));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(dragon).doesNotContain(otherBlocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
    }
}
