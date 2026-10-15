package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InversionBehemoth.class, GiantCockroach.class})
class InversionBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, switches any chosen creatures")
    void switchesChosenCreaturesAtBeginningOfCombat() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new InversionBehemoth());
        Permanent ownCockroach = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());
        Permanent opposingCockroach = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                player1.getId(), behemoth.getId(), ownCockroach.getId(), opposingCockroach.getId());

        harness.handlePermanentChosen(player1, ownCockroach.getId());
        harness.handlePermanentChosen(player1, opposingCockroach.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, ownCockroach)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCockroach)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCockroach)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCockroach)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's beginning of combat")
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new InversionBehemoth());
        Permanent opposingCockroach = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());

        advanceToBeginningOfCombat(player2);

        assertThat(gqs.getEffectivePower(gd, opposingCockroach)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingCockroach)).isEqualTo(2);
    }

    @Test
    @DisplayName("The switches wear off at cleanup")
    void switchesWearOffAtCleanup() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new InversionBehemoth());
        Permanent cockroach = harness.addToBattlefieldAndReturn(player1, new GiantCockroach());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, behemoth.getId());
        harness.handlePermanentChosen(player1, cockroach.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, cockroach)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cockroach)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, cockroach)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cockroach)).isEqualTo(2);
    }

    @Test
    @DisplayName("The controller may choose zero targets")
    void mayChooseZeroTargets() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new InversionBehemoth());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(9);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("A creature cannot be chosen twice for the same ability")
    void chosenCreatureIsRemovedFromRemainingChoices() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new InversionBehemoth());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new InversionBehemoth());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, behemoth.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(player1.getId(), other.getId());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(9);
    }

    @Test
    @DisplayName("Switching a creature in two combats during the same turn restores its original values")
    void switchesFromTwoCombatsCancel() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new InversionBehemoth());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, behemoth.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(2);

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, behemoth.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, behemoth)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, behemoth)).isEqualTo(9);
    }

    @Test
    @DisplayName("The remaining target is switched even if the source was also targeted and leaves")
    void resolvesForRemainingTargetAfterSourceLeaves() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new InversionBehemoth());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new InversionBehemoth());

        advanceToBeginningOfCombat(player1);
        harness.handlePermanentChosen(player1, behemoth.getId());
        harness.handlePermanentChosen(player1, other.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, behemoth);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Inversion Behemoth");
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Any number of targets includes more than one hundred creatures")
    void mayTargetMoreThanOneHundredCreatures() {
        List<Permanent> creatures = new ArrayList<>();
        creatures.add(harness.addToBattlefieldAndReturn(player1, new InversionBehemoth()));
        for (int i = 0; i < 100; i++) {
            creatures.add(harness.addToBattlefieldAndReturn(player2, new InversionBehemoth()));
        }

        advanceToBeginningOfCombat(player1);
        for (Permanent creature : creatures) {
            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.validIds()).contains(creature.getId());
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.passBothPriorities();

        for (Permanent creature : creatures) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        }
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }
}
