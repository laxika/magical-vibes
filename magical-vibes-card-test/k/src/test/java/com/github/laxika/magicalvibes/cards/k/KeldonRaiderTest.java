package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KeldonRaider.class, Forest.class, GrizzlyBears.class})
class KeldonRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("When Keldon Raider enters, accepting may prompts discard then draws a card")
    void acceptMayDiscardsThenDraws() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Card bearInHand = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new KeldonRaider(), bearInHand)));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        // Resolve the creature spell → Keldon Raider enters
        harness.passBothPriorities();
        // Resolve ETB triggered ability → MayEffect prompts player
        harness.passBothPriorities();

        // May ability prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Should now be awaiting discard choice (discard happens BEFORE draw)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        // Discard the remaining card (Grizzly Bears at index 0)
        harness.handleCardChosen(player1, 0);

        // The Grizzly Bears should be in the graveyard
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Should have drawn a card (Forest from deck) — hand size = 1
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("When Keldon Raider enters, declining may does not discard or draw")
    void declineMayDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Card bearInHand = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new KeldonRaider(), bearInHand)));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB → may prompt

        harness.handleMayAbilityChosen(player1, false);

        // Hand should still have the Grizzly Bears (no discard, no draw)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("When Keldon Raider enters with empty hand, accepting may does nothing (cannot discard)")
    void acceptMayWithEmptyHandDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, new ArrayList<>(List.of(new KeldonRaider())));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB → may prompt

        harness.handleMayAbilityChosen(player1, true);

        // Hand is empty — no discard possible, so no draw either
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller chooses one card to discard and draws exactly one card afterward")
    void choosesOneOfMultipleCardsToDiscard() {
        Card retainedCard = new KeldonRaider();
        Card discardedCard = new Forest();
        Card drawnCard = new Forest();
        Card nextCard = new KeldonRaider();
        harness.setLibrary(player1, List.of(drawnCard, nextCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new KeldonRaider(), retainedCard, discardedCard));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard, discardedCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard, nextCard);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard, drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discardedCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
