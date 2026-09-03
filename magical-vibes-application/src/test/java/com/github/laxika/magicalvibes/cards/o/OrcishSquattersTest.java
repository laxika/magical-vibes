package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcishSquatters.class, Forest.class, BalduvianBears.class})
class OrcishSquattersTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new OrcishSquatters());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addDefenderLand() {
        return harness.addToBattlefieldAndReturn(player2, new Forest());
    }

    private void advanceToMayChoice(Permanent target) {
        prepareDeclareBlockers();
        // Defender declares no blocks, so Orcish Squatters is unblocked and its trigger fires.
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting and choosing a land gains control of it and prevents combat damage")
    void acceptGainsControlAndPreventsDamage() {
        Permanent forest = addDefenderLand();
        Permanent otherForest = addDefenderLand();
        Permanent attacker = addAttacker();

        advanceToMayChoice(forest);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        // Player 1 now controls the land.
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(forest);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherForest);
        harness.assertLife(player2, 20);

        // The attacker assigns no combat damage this turn.
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("The combat-damage prevention wears off at end of turn")
    void preventionWearsOff() {
        Permanent forest = addDefenderLand();
        Permanent attacker = addAttacker();

        advanceToMayChoice(forest);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // POSTCOMBAT_MAIN -> END_STEP

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("The stolen land returns when Orcish Squatters leaves the battlefield")
    void controlEndsWhenSourceLeavesBattlefield() {
        Permanent forest = addDefenderLand();
        Permanent attacker = addAttacker();

        advanceToMayChoice(forest);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, attacker));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
    }

    @Test
    @DisplayName("Declining the may leaves the land with its controller and deals combat damage")
    void declineDoesNothing() {
        Permanent forest = addDefenderLand();
        Permanent attacker = addAttacker();

        advanceToMayChoice(forest);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The land target is chosen before the may decision")
    void targetIsChosenBeforeMayDecision() {
        Permanent forest = addDefenderLand();
        addCreatureReady(player2, new BalduvianBears());
        addAttacker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(forest.getId());

        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
    }

    @Test
    @DisplayName("Without a legal land target, the ability is not put on the stack")
    void noLandTargetMeansNoTrigger() {
        // Defender controls only a creature, no lands.
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        addAttacker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("A blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        addCreatureReady(player2, new BalduvianBears());
        addAttacker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
