package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StadiumTidalmage.class, Forest.class, GrizzlyBears.class})
class StadiumTidalmageTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield lets controller draw then discard (accept)")
    void entersAndLoots() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new StadiumTidalmage(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell → ETB trigger on stack
        harness.passBothPriorities(); // resolve the ETB trigger → MayEffect prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        // Drew a card, now awaiting discard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // Cast Tidalmage (hand -1), drew 1, discarded 1 → net -1 vs before
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
    }

    @Test
    @DisplayName("Declining the ETB loot draws nothing")
    void entersDeclineLoot() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new StadiumTidalmage(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        // Only the cast Tidalmage left hand; no draw
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
    }

    @Test
    @DisplayName("Attacking lets controller loot")
    void attacksAndLoots() {
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new StadiumTidalmage());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(0));

        // Attack trigger on stack
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Declining the attack loot leaves the hand and library unchanged")
    void attacksAndDeclinesLoot() {
        Forest drawnCard = new Forest();
        Forest heldCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(heldCard));
        harness.setGraveyard(player1, List.of());
        addCreatureReady(player1, new StadiumTidalmage());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(heldCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The card drawn by the entry trigger can be discarded from an initially empty hand")
    void entersAndDiscardsTheDrawnCard() {
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new StadiumTidalmage(), "{2}{U}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's attack trigger draws and discards only for that opponent")
    void opponentAttacksAndLoots() {
        Forest heldCard = new Forest();
        Forest drawnCard = new Forest();
        Forest otherPlayersCard = new Forest();
        harness.setHand(player1, List.of(otherPlayersCard));
        harness.setHand(player2, List.of(heldCard));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setGraveyard(player2, List.of());
        addCreatureReady(player2, new StadiumTidalmage());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(heldCard, drawnCard);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(heldCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherPlayersCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
