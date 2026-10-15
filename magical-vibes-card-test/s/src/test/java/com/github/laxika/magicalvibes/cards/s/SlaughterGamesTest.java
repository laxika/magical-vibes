package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlaughterGames.class, GrizzlyBears.class, Peek.class, Cancel.class})
class SlaughterGamesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting the opponent")
    void castingTargetsOpponent() {
        harness.setHand(player1, List.of(new SlaughterGames()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new SlaughterGames()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Exiles matching cards from the opponent's hand, graveyard, and library")
    void exilesMatchingCardsFromAllZones() {
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        Card bears3 = new GrizzlyBears();
        Card peek = new Peek();

        harness.setHand(player2, new ArrayList<>(List.of(bears1, peek)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears2)));
        harness.setLibrary(player2, List.of(bears3));

        harness.setHand(player1, List.of(new SlaughterGames()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId(), bears3.getId()));

        long exiledCount = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count();
        assertThat(exiledCount).isEqualTo(3);

        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertInHand(player2, "Peek");
    }

    @Test
    @DisplayName("Land card names are not offered")
    void doesNotOfferLandNames() {
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new SlaughterGames()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).noneMatch(name ->
                name.equals("Plains") || name.equals("Island") || name.equals("Swamp")
                        || name.equals("Mountain") || name.equals("Forest"));
    }

    @Test
    @DisplayName("Can't be countered — Cancel resolves but Slaughter Games still resolves")
    void cannotBeCountered() {
        Card bears = new GrizzlyBears();
        SlaughterGames games = new SlaughterGames();
        harness.setHand(player2, new ArrayList<>(List.of(bears, new Cancel())));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.setHand(player1, List.of(games));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 1, games.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertInGraveyard(player2, "Cancel");
        harness.assertInGraveyard(player1, "Slaughter Games");
    }

    @Test
    @DisplayName("May exile zero cards even when every searched zone has a match")
    void mayChooseZeroCards() {
        Card handCard = new SlaughterGames();
        Card graveyardCard = new SlaughterGames();
        Card libraryCard = new SlaughterGames();
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new SlaughterGames()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Slaughter Games");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Slaughter Games");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May exile only some matches and leaves the caster's cards untouched")
    void mayChooseOnlySomeMatches() {
        Card opponentHand = new SlaughterGames();
        Card opponentGraveyard = new SlaughterGames();
        Card opponentLibrary = new SlaughterGames();
        Card ownHand = new SlaughterGames();
        Card ownGraveyard = new SlaughterGames();
        Card ownLibrary = new SlaughterGames();
        harness.setHand(player2, List.of(opponentHand));
        harness.setGraveyard(player2, List.of(opponentGraveyard));
        harness.setLibrary(player2, List.of(opponentLibrary));
        harness.setHand(player1, List.of(new SlaughterGames(), ownHand));
        harness.setGraveyard(player1, List.of(ownGraveyard));
        harness.setLibrary(player1, List.of(ownLibrary));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Slaughter Games");
        harness.handleMultipleCardsChosen(player1, List.of(opponentHand.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentHand);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibrary);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownHand);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownGraveyard).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownLibrary);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can name an absent nonland card and finishes without an exile prompt")
    void mayNameCardAbsentFromTheGame() {
        harness.setHand(player2, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new SlaughterGames()));
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Cancel");

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Slaughter Games");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
