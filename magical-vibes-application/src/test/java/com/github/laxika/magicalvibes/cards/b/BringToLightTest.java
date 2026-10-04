package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.EndlessOne;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BringToLight.class, Divination.class, Forest.class, GrizzlyBears.class, Shock.class, BoneSplinters.class, Twincast.class, EndlessOne.class})
class BringToLightTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for eligible cards with mana value at most the Converge count")
    void searchesWithinConvergeManaValue() {
        castWithMana(List.of(new Forest(), new GrizzlyBears(), new Shock(), new Divination()),
                3, ManaColor.GREEN, ManaColor.BLUE);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);

        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Shock");
    }

    @Test
    @DisplayName("Three colors of mana increase the search limit to three")
    void countsThreeDistinctColors() {
        castWithMana(List.of(new GrizzlyBears(), new Divination()),
                0, ManaColor.GREEN, ManaColor.BLUE,
                ManaColor.BLACK, ManaColor.BLACK, ManaColor.BLACK);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);

        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Divination");
    }

    @Test
    @DisplayName("Exiles the chosen card and offers it for a free cast")
    void exilesAndCastsChosenCardWithoutPaying() {
        castWithMana(List.of(new GrizzlyBears()), 3, ManaColor.GREEN, ManaColor.BLUE);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void decliningLeavesCardExiled() {
        castWithMana(List.of(new GrizzlyBears()), 3, ManaColor.GREEN, ManaColor.BLUE);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void mayFailToFind() {
        castWithMana(List.of(new GrizzlyBears()), 3, ManaColor.GREEN, ManaColor.BLUE);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void freeSorceryResolvesAndGoesToGraveyard() {
        castWithMana(List.of(new Divination(), new Forest(), new Forest()), 0,
                ManaColor.GREEN, ManaColor.BLUE, ManaColor.BLACK, ManaColor.BLACK, ManaColor.BLACK);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest", "Forest");
        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void fiveColorsAllowFiveManaCard() {
        castWithMana(List.of(new BringToLight(), new Forest()), 0,
                ManaColor.GREEN, ManaColor.BLUE, ManaColor.BLACK, ManaColor.RED, ManaColor.WHITE);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Bring to Light");
    }

    @Test
    void copyDoesNotInheritColorsSpent() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new EndlessOne()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new EndlessOne()));
        harness.setHand(player1, List.of(new BringToLight()));
        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 0);
        var originalId = gd.stack.getFirst().getCard().getId();
        harness.passPriority(player1);
        harness.castInstant(player2, 0, originalId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Endless One");
    }

    @Test
    void freeCastPaysMandatorySacrificeCost() {
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castWithMana(List.of(new BoneSplinters()), 3, ManaColor.GREEN, ManaColor.BLUE);
        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, creature.getId());
        }
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void castWithMana(List<Card> library, int colorlessMana, ManaColor... colors) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new BringToLight()));
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
        for (ManaColor color : colors) {
            harness.addMana(player1, color, 1);
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
