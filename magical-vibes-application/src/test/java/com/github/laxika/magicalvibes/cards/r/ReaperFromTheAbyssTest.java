package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReaperFromTheAbyss.class, GrizzlyBears.class, Shock.class,
        WalkingCorpse.class, BrimstoneVolley.class})
class ReaperFromTheAbyssTest extends BaseCardTest {

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
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the opponent's Grizzly Bears as target
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Bears should be destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
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
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        // Bears should still be alive
        harness.assertOnBattlefield(player2, "Grizzly Bears");
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
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        // Both Reapers should still be alive
        harness.assertOnBattlefield(player1, "Reaper from the Abyss");
        harness.assertOnBattlefield(player2, "Reaper from the Abyss");
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

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose own Grizzly Bears as target
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.passBothPriorities();

        // Own Bears should be destroyed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
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
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        // Bears destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Integration: actual creature death via Shock enables morbid at end step")
    void actualCreatureDeathEnablesMorbidAtEndStep() {
        harness.addToBattlefield(player1, new ReaperFromTheAbyss());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Create another target for Reaper (since Bears will be dead)
        Permanent remainingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the remaining creature as target
        harness.handlePermanentChosen(player1, remainingBear.getId());
        harness.passBothPriorities();

        // The remaining Bear should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(remainingBear.getId()));
    }

    @Test
    @DisplayName("A creature dying after the end step begins does not create a morbid trigger")
    void deathDuringEndStepDoesNotTrigger() {
        harness.addToBattlefield(player1, new ReaperFromTheAbyss());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.setHand(player1, List.of(new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertOnBattlefield(player2, "Walking Corpse");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Multiple deaths produce only one trigger per Reaper at the end step")
    void multipleDeathsProduceOneTrigger() {
        harness.addToBattlefield(player1, new ReaperFromTheAbyss());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.addToBattlefield(player2, new WalkingCorpse());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 3, Integer::sum);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The morbid ability still resolves after its source dies")
    void abilityResolvesAfterSourceDies() {
        Permanent reaper = harness.addToBattlefieldAndReturn(player1, new ReaperFromTheAbyss());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player1, List.of(new BrimstoneVolley(), new BrimstoneVolley()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castInstant(player1, 0, reaper.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, reaper.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Reaper from the Abyss");
        harness.assertOnBattlefield(player2, "Walking Corpse");

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
    }
}
