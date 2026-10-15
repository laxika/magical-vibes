package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.cards.g.GhirapurGearcrafter;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfiniteObliteration.class, TimberpackWolf.class, FieryImpulse.class, GhirapurGearcrafter.class})
class InfiniteObliterationTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new InfiniteObliteration()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Only creature card names are offered")
    void offersOnlyCreatureNames() {
        harness.setHand(player2, List.of(new TimberpackWolf(), new FieryImpulse()));

        harness.setHand(player1, List.of(new InfiniteObliteration()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Timberpack Wolf");
        assertThat(choice.options()).doesNotContain("Fiery Impulse");
    }

    @Test
    @DisplayName("Exiles matching creatures from the opponent's hand, graveyard, and library")
    void exilesMatchingCreaturesFromAllZones() {
        Card bears1 = new TimberpackWolf();
        Card bears2 = new TimberpackWolf();
        Card bears3 = new TimberpackWolf();

        harness.setHand(player2, List.of(bears1));
        harness.setGraveyard(player2, List.of(bears2));
        harness.setLibrary(player2, List.of(bears3));

        harness.setHand(player1, List.of(new InfiniteObliteration()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Timberpack Wolf");
        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId(), bears2.getId(), bears3.getId()));

        long exiledCount = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Timberpack Wolf"))
                .count();
        assertThat(exiledCount).isEqualTo(3);

        harness.assertNotInHand(player2, "Timberpack Wolf");
        harness.assertNotInGraveyard(player2, "Timberpack Wolf");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Timberpack Wolf"));
    }

    @Test
    @DisplayName("Goes to the caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new InfiniteObliteration()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Timberpack Wolf");

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Infinite Obliteration");
    }

    @Test
    @DisplayName("May leave all matching cards in their original zones")
    void mayExileZeroCards() {
        Card handCard = new TimberpackWolf();
        Card graveyardCard = new TimberpackWolf();
        Card libraryCard = new TimberpackWolf();
        harness.setHand(player2, List.of(handCard));
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new InfiniteObliteration()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Timberpack Wolf");
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Infinite Obliteration");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Exiles only selected copies from the targeted opponent's searched zones")
    void exilesOnlySelectedCopies() {
        Card selected = new TimberpackWolf();
        Card unselected = new TimberpackWolf();
        Card otherName = new FieryImpulse();
        Card ownCopy = new TimberpackWolf();
        harness.setHand(player2, List.of(selected, otherName));
        harness.setGraveyard(player2, List.of(unselected));
        harness.setLibrary(player2, List.of(new TimberpackWolf()));
        harness.addToBattlefield(player2, new TimberpackWolf());
        harness.setHand(player1, List.of(new InfiniteObliteration(), ownCopy));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Timberpack Wolf");
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(selected);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(otherName);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unselected);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCopy);
        harness.assertOnBattlefield(player2, "Timberpack Wolf");
        harness.assertInGraveyard(player1, "Infinite Obliteration");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Token names that are not actual creature card names cannot be chosen")
    void doesNotOfferTokenOnlyNames() {
        harness.enterBattlefieldAndReturn(player2, new GhirapurGearcrafter());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());

        harness.setHand(player1, List.of(new InfiniteObliteration()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Ghirapur Gearcrafter").doesNotContain("Thopter");
        assertThatThrownBy(() -> harness.handleListChoice(player1, "Thopter"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
