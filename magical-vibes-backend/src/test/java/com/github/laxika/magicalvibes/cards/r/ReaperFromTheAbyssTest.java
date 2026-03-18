package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MorbidConditionalEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReaperFromTheAbyssTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has END_STEP_TRIGGERED effect with MorbidConditionalEffect wrapping DestroyTargetPermanentEffect")
    void hasCorrectStructure() {
        ReaperFromTheAbyss card = new ReaperFromTheAbyss();

        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(MorbidConditionalEffect.class);

        MorbidConditionalEffect morbid =
                (MorbidConditionalEffect) card.getEffects(EffectSlot.END_STEP_TRIGGERED).getFirst();
        assertThat(morbid.wrapped()).isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    // ===== Morbid trigger at end step =====

    @Test
    @DisplayName("Destroys target non-Demon creature at end step when morbid is met")
    void destroysNonDemonCreatureAtEndStep() {
        harness.addToBattlefield(player1, new ReaperFromTheAbyss());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Set morbid condition
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step → triggers morbid end step ability
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        // Should be awaiting target selection
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the opponent's Grizzly Bears as target
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Bears should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not trigger when morbid is not met (no creature died this turn)")
    void doesNotTriggerWithoutMorbid() {
        harness.addToBattlefield(player1, new ReaperFromTheAbyss());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // No creature deaths this turn

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step
        harness.passBothPriorities();

        // No target selection should be prompted
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Bears should still be alive
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target Demon creatures")
    void cannotTargetDemonCreatures() {
        harness.addToBattlefield(player1, new ReaperFromTheAbyss());
        // Add another Reaper (a Demon) as the only other creature
        harness.addToBattlefield(player2, new ReaperFromTheAbyss());

        // Set morbid condition
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step → should trigger but have no valid targets (only Demons)
        harness.passBothPriorities();

        // No target selection since there are no valid non-Demon creatures
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Both Reapers should still be alive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Reaper from the Abyss"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Reaper from the Abyss"));
    }

    @Test
    @DisplayName("Can target own non-Demon creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new ReaperFromTheAbyss());
        harness.addToBattlefield(player1, new GrizzlyBears());

        // Set morbid condition
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose own Grizzly Bears as target
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.passBothPriorities();

        // Own Bears should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Triggers at each end step (including opponent's)")
    void triggersAtEachEndStep() {
        harness.addToBattlefield(player1, new ReaperFromTheAbyss());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Set morbid condition
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        // Advance to end step during opponent's turn
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        // Player 1 (Reaper controller) should be prompted for target
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        // Bears destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Integration: actual creature death via Shock enables morbid at end step")
    void actualCreatureDeathEnablesMorbidAtEndStep() {
        harness.addToBattlefield(player1, new ReaperFromTheAbyss());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Create another target for Reaper (since Bears will be dead)
        Permanent elk = new Permanent(new GrizzlyBears());
        elk.getCard().setName("Runeclaw Bear");
        gd.playerBattlefields.get(player2.getId()).add(elk);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Kill Bears with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // resolve Shock → Bears die → morbid is active

        // Now advance to end step
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose the remaining creature as target
        harness.handlePermanentChosen(player1, elk.getId());
        harness.passBothPriorities();

        // The elk should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(elk.getId()));
    }
}
