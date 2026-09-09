package com.github.laxika.magicalvibes.cards.o;

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

@CardUsed({OverlordOfTheFloodpits.class})
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
