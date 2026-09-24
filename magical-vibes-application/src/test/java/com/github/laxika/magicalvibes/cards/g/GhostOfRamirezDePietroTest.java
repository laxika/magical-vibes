package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HorrifyingRevelation;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostOfRamirezDePietro.class, GrizzlyBears.class, HillGiant.class, HorrifyingRevelation.class})
class GhostOfRamirezDePietroTest extends BaseCardTest {

    @Test
    @DisplayName("Ghost of Ramirez DePietro cannot be blocked by creatures with toughness 3 or greater")
    void cannotBeBlockedByLargeCreature() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent ghost = addCreatureReady(player1, new GhostOfRamirezDePietro());
        ghost.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int ghostIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ghost);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, ghostIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Ghost of Ramirez DePietro can be blocked by a creature with toughness less than 3")
    void canBeBlockedBySmallCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent ghost = addCreatureReady(player1, new GhostOfRamirezDePietro());
        ghost.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int ghostIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ghost);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, ghostIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Combat damage returns a discarded or milled card from any graveyard")
    void returnsDiscardedOrMilledCard() {
        Card discarded = new GrizzlyBears();
        Card milled = new HillGiant();
        Card fromBeforeThisTurn = new GrizzlyBears();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(milled));
        harness.setGraveyard(player1, List.of(fromBeforeThisTurn));

        harness.setHand(player1, List.of(new HorrifyingRevelation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        Permanent ghost = addCreatureReady(player1, new GhostOfRamirezDePietro());
        ghost.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(discarded.getId(), milled.getId());

        harness.handleMultipleCardsChosen(player1, List.of(milled.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(milled);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(fromBeforeThisTurn);
    }
}
