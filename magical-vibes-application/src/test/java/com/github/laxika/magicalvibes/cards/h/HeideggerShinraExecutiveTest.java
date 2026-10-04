package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeideggerShinraExecutive.class, GrizzlyBears.class, YotianSoldier.class, Humble.class})
class HeideggerShinraExecutiveTest extends BaseCardTest {

    @Test
    void boostsTargetCreatureByTheNumberOfSoldiersYouControl() {
        Permanent heidegger = addCreatureReady(player1, new HeideggerShinraExecutive());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new YotianSoldier());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(heidegger.getId(), target.getId())
                .doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void createsOneSoldierForEachOpponentWithMoreCreatures() {
        harness.addToBattlefield(player1, new HeideggerShinraExecutive());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
    }

    @Test
    void createsNoSoldiersWhenNoOpponentControlsMoreCreatures() {
        harness.addToBattlefield(player1, new HeideggerShinraExecutive());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    void countsSoldiersAtResolutionAndDoesNotCountOpposingSoldiers() {
        addCreatureReady(player1, new HeideggerShinraExecutive());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new YotianSoldier());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.addToBattlefield(player1, new YotianSoldier());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.addToBattlefield(player1, new YotianSoldier());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
    }

    @Test
    void boostStillResolvesAfterHeideggerLeavesAndDoesNotCountHim() {
        Permanent heidegger = addCreatureReady(player1, new HeideggerShinraExecutive());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new YotianSoldier());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(heidegger);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void canBoostHeideggerAndTheBoostExpiresAtEndOfTurn() {
        Permanent heidegger = addCreatureReady(player1, new HeideggerShinraExecutive());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, heidegger.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, heidegger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, heidegger)).isEqualTo(3);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, heidegger)).isEqualTo(3);
    }

    @Test
    void endStepCountsCreaturesAtResolutionRatherThanWhenTriggered() {
        harness.addToBattlefield(player1, new HeideggerShinraExecutive());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
    }

    @Test
    void neitherAbilityTriggersDuringOpponentsTurn() {
        Permanent heidegger = addCreatureReady(player1, new HeideggerShinraExecutive());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, heidegger)).isEqualTo(3);

        advanceToEndStep(player2);

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    @CardUsed({HeideggerShinraExecutive.class, Humble.class})
    void combatAbilityDoesNotTriggerAfterHeideggerLosesAllAbilities() {
        Permanent heidegger = addCreatureReady(player1, new HeideggerShinraExecutive());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, heidegger.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasLostAllAbilities(gd, heidegger)).isTrue();

        advanceToBeginningOfCombat(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, heidegger)).isZero();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
