package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaladrielElvenQueen.class, ArborElf.class, Forest.class, BirdsOfParadise.class, MaskwoodNexus.class})
class GaladrielElvenQueenTest extends BaseCardTest {

    @Test
    void dominionMajorityTemptsTheRingAndCountersTheChosenRingBearer() {
        Permanent galadriel = harness.addToBattlefieldAndReturn(player1, new GaladrielElvenQueen());
        harness.enterBattlefieldAndReturn(player1, new ArborElf());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.GaladrielElvenQueenChoice.DOMINION);
        harness.handleListChoice(player2, ChoiceContext.GaladrielElvenQueenChoice.DOMINION);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, galadriel.getId());

        assertThat(gd.ringLevels).containsEntry(player1.getId(), 1);
        assertThat(gd.ringBearerIds).containsEntry(player1.getId(), galadriel.getId());
        assertThat(galadriel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void guidanceWinsOnTieAndDrawsACard() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addToBattlefield(player1, new GaladrielElvenQueen());
        harness.enterBattlefieldAndReturn(player1, new ArborElf());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        harness.handleListChoice(player1, ChoiceContext.GaladrielElvenQueenChoice.DOMINION);
        harness.handleListChoice(player2, ChoiceContext.GaladrielElvenQueenChoice.GUIDANCE);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest).hasSize(handSizeBefore + 1);
        assertThat(gd.ringLevels).doesNotContainKey(player1.getId());
    }

    @Test
    void doesNotTriggerWithoutAnotherElfEnteringThisTurn() {
        harness.addToBattlefield(player1, new GaladrielElvenQueen());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void guidanceMajorityDrawsExactlyOneCard() {
        ArborElf drawnCard = new ArborElf();
        harness.setLibrary(player1, List.of(drawnCard, new ArborElf()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addToBattlefield(player1, new GaladrielElvenQueen());
        harness.enterBattlefieldAndReturn(player1, new ArborElf());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.GaladrielElvenQueenChoice.GUIDANCE);
        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.GaladrielElvenQueenChoice.GUIDANCE);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard).hasSize(handSizeBefore + 1);
        assertThat(gd.ringLevels).doesNotContainKey(player1.getId());
    }

    @Test
    void galadrielsOwnEntryDoesNotSatisfyAnotherElfCondition() {
        harness.enterBattlefieldAndReturn(player1, new GaladrielElvenQueen());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsElfEntryDoesNotSatisfyCondition() {
        harness.addToBattlefield(player1, new GaladrielElvenQueen());
        harness.enterBattlefieldAndReturn(player2, new ArborElf());

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new GaladrielElvenQueen());
        harness.enterBattlefieldAndReturn(player1, new ArborElf());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void elfThatEnteredBeforeGaladrielStillQualifies() {
        harness.enterBattlefieldAndReturn(player1, new ArborElf());
        harness.enterBattlefieldAndReturn(player1, new GaladrielElvenQueen());
        ArborElf drawnCard = new ArborElf();
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.GaladrielElvenQueenChoice.GUIDANCE);
        harness.handleListChoice(player2, ChoiceContext.GaladrielElvenQueenChoice.GUIDANCE);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void elfEnteringAfterCombatBeginsDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new GaladrielElvenQueen());
        advanceToBeginningOfCombat(player1);

        harness.enterBattlefieldAndReturn(player1, new ArborElf());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void dominionCountersNewRingBearerInsteadOfPreviousRingBearer() {
        Permanent galadriel = harness.addToBattlefieldAndReturn(player1, new GaladrielElvenQueen());
        Permanent elf = harness.enterBattlefieldAndReturn(player1, new ArborElf());
        gd.ringLevels.put(player1.getId(), 4);
        gd.ringBearerIds.put(player1.getId(), galadriel.getId());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.GaladrielElvenQueenChoice.DOMINION);
        harness.handleListChoice(player2, ChoiceContext.GaladrielElvenQueenChoice.DOMINION);
        harness.handlePermanentChosen(player1, elf.getId());

        assertThat(gd.ringLevels).containsEntry(player1.getId(), 4);
        assertThat(gd.ringBearerIds).containsEntry(player1.getId(), elf.getId());
        assertThat(elf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(galadriel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void creatureEnteringAsElfDueToMaskwoodNexusQualifies() {
        harness.addToBattlefield(player1, new GaladrielElvenQueen());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.enterBattlefieldAndReturn(player1, new BirdsOfParadise());
        ArborElf drawnCard = new ArborElf();
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToBeginningOfCombat(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.GaladrielElvenQueenChoice.GUIDANCE);
        harness.handleListChoice(player2, ChoiceContext.GaladrielElvenQueenChoice.GUIDANCE);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    private PendingInteraction.ColorChoice activeVote() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.GaladrielElvenQueenChoice.OPTIONS);
        return choice;
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
