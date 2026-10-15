package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlissasRetriever.class, GrizzlyBears.class, HillGiant.class})
class GlissasRetrieverTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by a creature with power 2 or less")
    void cannotBeBlockedBySmallCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent retriever = addCreatureReady(player1, new GlissasRetriever());
        retriever.setAttacking(true);

        beginBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(retriever);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with power 3 or greater")
    void canBeBlockedByLargeCreature() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent retriever = addCreatureReady(player1, new GlissasRetriever());
        retriever.setAttacking(true);

        beginBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(retriever);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Exiles itself and returns up to the number of sufficiently poisoned opponents")
    void exilesItselfAndReturnsPoisonScaledTargets() {
        Permanent retriever = harness.addToBattlefieldAndReturn(player1, new GlissasRetriever());
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        harness.setGraveyard(player1, List.of(first, second));
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, retriever));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(retriever.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(first.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Does not return cards when no opponent has three poison counters")
    void noReturnWithoutPoisonThreshold() {
        Permanent retriever = harness.addToBattlefieldAndReturn(player1, new GlissasRetriever());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        gd.playerPoisonCounters.put(player2.getId(), 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, retriever));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(retriever.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
