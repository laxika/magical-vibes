package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TruthOrTale.class, Shock.class, GiantGrowth.class, GrizzlyBears.class})
class TruthOrTaleTest extends BaseCardTest {

    @Test
    @DisplayName("controller separates, opponent chooses a pile, and controller chooses one card")
    void opponentChoosesPileThenControllerChoosesOneCard() {
        Card shock = new Shock();
        Card giantGrowth = new GiantGrowth();
        Card bears = new GrizzlyBears();
        Card secondShock = new Shock();
        Card secondGrowth = new GiantGrowth();
        harness.setLibrary(player1, List.of(shock, giantGrowth, bears, secondShock, secondGrowth));

        cast();

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), giantGrowth.getId()));
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.MultiGraveyardChoice cardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(cardChoice.playerId()).isEqualTo(player1.getId());
        assertThat(cardChoice.validCardIds()).containsExactly(shock.getId(), giantGrowth.getId());

        harness.handleMultipleCardsChosen(player1, List.of(giantGrowth.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));

        assertThat(gd.playerHands.get(player1.getId())).contains(giantGrowth);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(shock, bears, secondShock, secondGrowth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears, secondShock, secondGrowth, shock);
    }

    @Test
    @DisplayName("the opponent may choose the other pile")
    void opponentChoosesOtherPile() {
        Card shock = new Shock();
        Card giantGrowth = new GiantGrowth();
        Card bears = new GrizzlyBears();
        Card secondShock = new Shock();
        Card secondGrowth = new GiantGrowth();
        harness.setLibrary(player1, List.of(shock, giantGrowth, bears, secondShock, secondGrowth));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), giantGrowth.getId()));
        harness.handleMayAbilityChosen(player2, false);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 3, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondShock, secondGrowth, giantGrowth, shock);
    }

    @Test
    @DisplayName("choosing an empty pile skips the card choice and bottoms every revealed card")
    void choosingEmptyPileBottomsEveryRevealedCard() {
        Card shock = new Shock();
        Card giantGrowth = new GiantGrowth();
        Card bears = new GrizzlyBears();
        Card secondShock = new Shock();
        Card secondGrowth = new GiantGrowth();
        harness.setLibrary(player1, List.of(shock, giantGrowth, bears, secondShock, secondGrowth));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(
                shock.getId(), giantGrowth.getId(), bears.getId(), secondShock.getId(), secondGrowth.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(4, 3, 2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(secondGrowth, secondShock, bears, giantGrowth, shock);
    }

    @Test
    @DisplayName("reveals all available cards when the library has fewer than five")
    void revealsAllAvailableCardsWhenLibraryIsShorterThanFive() {
        Card shock = new Shock();
        Card giantGrowth = new GiantGrowth();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, giantGrowth, bears));

        cast();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.MultiGraveyardChoice cardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(cardChoice).isNotNull();
        assertThat(cardChoice.playerId()).isEqualTo(player1.getId());
        assertThat(cardChoice.validCardIds()).containsExactly(shock.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears, giantGrowth);
    }

    @Test
    @DisplayName("in multiplayer, the controller chooses which opponent chooses the pile")
    void choosesAnOpponentInMultiplayer() {
        addThirdPlayer();
        Card shock = new Shock();
        Card giantGrowth = new GiantGrowth();
        Card bears = new GrizzlyBears();
        Card secondShock = new Shock();
        Card secondGrowth = new GiantGrowth();
        harness.setLibrary(player1, List.of(shock, giantGrowth, bears, secondShock, secondGrowth));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
    }

    @Test
    @DisplayName("an empty library resolves without a choice or a card in hand")
    void emptyLibraryResolvesWithoutChoices() {
        harness.setLibrary(player1, List.of());

        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("the only library card goes to hand when its pile is chosen")
    void takesOnlyLibraryCardWithoutReordering() {
        Card onlyCard = new TruthOrTale();
        harness.setLibrary(player1, List.of(onlyCard));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("choosing the empty pile returns the only library card to the bottom")
    void bottomsOnlyLibraryCardWithoutReordering() {
        Card onlyCard = new TruthOrTale();
        harness.setLibrary(player1, List.of(onlyCard));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("only the top five are revealed and the remainder stays above the bottomed cards")
    void preservesUnrevealedLibraryAboveReorderedCards() {
        Card first = new TruthOrTale();
        Card second = new TruthOrTale();
        Card third = new TruthOrTale();
        Card fourth = new TruthOrTale();
        Card fifth = new TruthOrTale();
        Card sixth = new TruthOrTale();
        Card seventh = new TruthOrTale();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth, seventh));

        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(
                first.getId(), second.getId(), third.getId(), fourth.getId(), fifth.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth, seventh);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(sixth, seventh, first, fifth, fourth, third);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(first.getId())
                        || card.getId().equals(third.getId())
                        || card.getId().equals(fourth.getId())
                        || card.getId().equals(fifth.getId()));
    }

    private void cast() {
        harness.castFromHand(player1, new TruthOrTale(), "{1}{U}");
        harness.passBothPriorities();
    }

    private void addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
    }
}
