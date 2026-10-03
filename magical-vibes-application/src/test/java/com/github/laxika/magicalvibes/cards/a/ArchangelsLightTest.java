package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchangelsLight.class, GrizzlyBears.class, GiantSpider.class, DawntreaderElk.class})
class ArchangelsLightTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each card in graveyard")
    void gains2LifePerGraveyardCard() {
        Card bear1 = new GrizzlyBears();
        Card bear2 = new GrizzlyBears();
        Card spider = new GiantSpider();
        harness.setGraveyard(player1, List.of(bear1, bear2, spider));
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        // 3 cards in graveyard × 2 life = 6 life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    @Test
    @DisplayName("Gains no life when graveyard is empty")
    void gainsNoLifeWithEmptyGraveyard() {
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Shuffles graveyard into library after gaining life")
    void shufflesGraveyardIntoLibrary() {
        Card bear1 = new GrizzlyBears();
        Card bear2 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear1, bear2));
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);

        // Graveyard should only contain Archangel's Light itself (sorcery goes to graveyard after resolution)
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        // Library should have 2 more cards (the bears)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 2);
    }

    @Test
    @DisplayName("Life gain counts cards before shuffle")
    void lifeGainCountsCardsBeforeShuffle() {
        // 5 cards in graveyard
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GiantSpider(), new GiantSpider()));
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, 0);

        // 5 cards × 2 life = 10 life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 10);
        // Graveyard shuffled into library (only Archangel's Light remains in graveyard)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears") || c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Archangel's Light goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setHand(player1, List.of(new ArchangelsLight()));
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Archangel's Light");
    }

    @Test
    @DisplayName("Counts all card types and leaves the opponent's zones and life unchanged")
    void countsMixedCardsOnlyInControllersGraveyard() {
        Card elk = new DawntreaderElk();
        Card previousLight = new ArchangelsLight();
        Card opponentCard = new DawntreaderElk();
        Card spell = new ArchangelsLight();
        harness.setGraveyard(player1, List.of(elk, previousLight));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 8);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        List<Card> libraryBefore = new ArrayList<>(gd.playerDecks.get(player1.getId()));
        List<Card> opponentLibraryBefore = new ArrayList<>(gd.playerDecks.get(player2.getId()));

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, lifeBefore + 4);
        harness.assertLife(player2, opponentLifeBefore);
        libraryBefore.addAll(List.of(elk, previousLight));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(libraryBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibraryBefore);
    }

    @Test
    @DisplayName("Uses the graveyard contents at resolution rather than at casting")
    void countsGraveyardAtResolution() {
        Card removedCard = new DawntreaderElk();
        Card addedCard = new ArchangelsLight();
        Card addedElk = new DawntreaderElk();
        Card spell = new ArchangelsLight();
        harness.setGraveyard(player1, List.of(removedCard));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.WHITE, 8);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, 0);
        harness.setGraveyard(player1, List.of(addedCard, addedElk));
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 4);
        assertThat(gd.playerDecks.get(player1.getId())).contains(addedCard, addedElk).doesNotContain(removedCard, spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }
}
