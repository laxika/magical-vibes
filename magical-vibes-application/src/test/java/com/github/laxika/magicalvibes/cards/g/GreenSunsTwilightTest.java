package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cankerbloom;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreenSunsTwilight.class, Cankerbloom.class, Forest.class, TitanicGrowth.class})
class GreenSunsTwilightTest extends BaseCardTest {

    @Test
    @DisplayName("With X less than 5, puts the chosen creature and land into hand")
    void putsCardsIntoHandBelowThreshold() {
        resolveSpell(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .contains("Cankerbloom", "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With X at least 5, the hand mode puts both chosen cards into hand")
    void highXHandMode() {
        resolveSpell(5);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player1, "Put the chosen cards into your hand");

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .contains("Cankerbloom", "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With X at least 5, the battlefield mode puts both chosen cards onto the battlefield")
    void highXBattlefieldMode() {
        resolveSpell(5);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player1, "Put the chosen cards onto the battlefield");

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().map(p -> p.getCard().getName()))
                .containsExactlyInAnyOrder("Cankerbloom", "Forest");
        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .doesNotContain("Cankerbloom", "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void revealsCardsBeforeChoosingHighXDestination() {
        resolveSpell(5);

        assertThat(gameLogContains("reveals Cankerbloom, Forest, Titanic Growth, Titanic Growth from the top"))
                .isTrue();
    }

    @Test
    void zeroXRevealsExactlyOneCard() {
        castSpell(0, List.of(new Cankerbloom(), new Forest()));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Cankerbloom");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotDeclineOnlyEligibleCard() {
        castSpell(0, List.of(new Cankerbloom(), new Forest()));

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseOnlyLandWhenBothTypesAreRevealed() {
        resolveSpell(4);
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Forest");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Cankerbloom", "Titanic Growth", "Titanic Growth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canChooseOnlyCreatureWhenBothTypesAreRevealed() {
        resolveSpell(4);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Cankerbloom");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Forest", "Titanic Growth", "Titanic Growth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void unchosenCardsGoBelowUnrevealedCards() {
        castSpell(1, List.of(new Cankerbloom(), new TitanicGrowth(), new Forest()));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Cankerbloom");
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Forest", "Titanic Growth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noEligibleCardsAreReturnedToBottomWithoutAChoice() {
        castSpell(0, List.of(new TitanicGrowth(), new Forest()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Forest", "Titanic Growth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotRequireAPick() {
        castSpell(0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void resolveSpell(int x) {
        castSpell(x, List.of(new Cankerbloom(), new Forest(), new TitanicGrowth(), new TitanicGrowth()));

        if (x < 5) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        }
    }

    private void castSpell(int x, List<Card> library) {
        harness.setHand(player1, List.of(new GreenSunsTwilight()));
        harness.addMana(player1, ManaColor.GREEN, x + 1);
        harness.setLibrary(player1, library);
        harness.castSorceryForX(player1, 0, x, Map.of());
        harness.passBothPriorities();
    }
}
