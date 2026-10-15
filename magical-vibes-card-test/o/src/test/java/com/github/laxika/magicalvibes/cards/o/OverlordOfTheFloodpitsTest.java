package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OverlordOfTheFloodpits.class, MycosynthLattice.class})
class OverlordOfTheFloodpitsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws two cards, then discards one")
    void enteringDrawsTwoAndDiscardsOne() {
        harness.setHand(player1, List.of(new OverlordOfTheFloodpits()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.playerId()).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, discard.validIndices().getFirst());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Overlord of the Floodpits");
    }

    @Test
    @DisplayName("Casting with impending enters with four time counters and is not a creature")
    void impendingCastEntersWithCountersAndIsNotCreature() {
        Permanent overlord = castWithImpending();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    void impendingPreservesOtherCardTypes() {
        harness.addToBattlefield(player1, new MycosynthLattice());

        Permanent overlord = castWithImpending();

        assertThat(gqs.isCreature(gd, overlord)).isFalse();
        assertThat(gqs.isEnchantment(gd, overlord)).isTrue();
        assertThat(gqs.isArtifact(gd, overlord)).isTrue();
    }

    @Test
    void opponentsEndStepDoesNotRemoveTimeCounter() {
        Permanent overlord = castWithImpending();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ownEndStepRemovesExactlyOneTimeCounter() {
        Permanent overlord = castWithImpending();

        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    void timeCounterAddedAfterLastCounterRestoresImpendingRestriction() {
        Permanent overlord = castWithImpending();
        overlord.setCounterCount(CounterType.TIME, 1);
        advanceToOwnEndStep();
        assertThat(gqs.isCreature(gd, overlord)).isTrue();

        overlord.setCounterCount(CounterType.TIME, 1);

        assertThat(gqs.isCreature(gd, overlord)).isFalse();
    }

    @Test
    void normalCastDoesNotHaveImpendingRestrictionsEvenWithTimeCounter() {
        harness.setHand(player1, List.of(new OverlordOfTheFloodpits()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, discard.validIndices().getFirst());
        Permanent overlord = findPermanent(player1, "Overlord of the Floodpits");
        assertThat(overlord.getCounterCount(CounterType.TIME)).isZero();
        overlord.setCounterCount(CounterType.TIME, 1);

        advanceToOwnEndStep();

        assertThat(gqs.isCreature(gd, overlord)).isTrue();
        assertThat(overlord.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing the last impending counter makes it a creature")
    void lastCounterMakesItCreature() {
        Permanent overlord = castWithImpending();
        overlord.setCounterCount(CounterType.TIME, 1);

        advanceToOwnEndStep();

        assertThat(overlord.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.isCreature(gd, overlord)).isTrue();
    }

    @Test
    @DisplayName("Attacking draws two cards, then discards one")
    void attackingDrawsTwoAndDiscardsOne() {
        Permanent overlord = addCreatureReady(player1, new OverlordOfTheFloodpits());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, discard.validIndices().getFirst());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(overlord.isAttackedThisTurn()).isTrue();
    }

    private Permanent castWithImpending() {
        harness.setHand(player1, List.of(new OverlordOfTheFloodpits()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, discard.validIndices().getFirst());

        return findPermanent(player1, "Overlord of the Floodpits");
    }

    private void advanceToOwnEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
