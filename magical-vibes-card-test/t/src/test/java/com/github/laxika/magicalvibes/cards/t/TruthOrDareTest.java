package com.github.laxika.magicalvibes.cards.t;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({TruthOrDare.class, Forest.class})
class TruthOrDareTest extends BaseCardTest {

    private static final String TRUTH = "Truth";
    private static final String DARE = "Dare";

    @Test
    @DisplayName("The targeted opponent chooses Truth or Dare")
    void targetedOpponentChoosesMode() {
        castTruthOrDare();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(TRUTH, DARE);
    }

    @Test
    @DisplayName("Truth reveals the targeted opponent's hand for the rest of the game")
    void truthRevealsTargetHandForRestOfGame() {
        harness.setHand(player2, List.of(new Forest()));
        castTruthOrDare();

        harness.handleListChoice(player2, TRUTH);

        assertThat(player1SeesOpponentHand()).isTrue();
        harness.setHand(player2, List.of(new Forest()));
        assertThat(player1SeesOpponentHand()).isTrue();
    }

    @Test
    @DisplayName("Dare mills the targeted opponent's library down to ten cards")
    void dareMillsAllButBottomTen() {
        harness.setLibrary(player2, cards(12));
        castTruthOrDare();

        harness.handleListChoice(player2, DARE);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Dare mills nothing when the targeted opponent has ten or fewer cards")
    void dareMillsNothingAtTenCards() {
        harness.setLibrary(player2, cards(10));
        castTruthOrDare();

        harness.handleListChoice(player2, DARE);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Truth or Dare cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new TruthOrDare()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Dare preserves the bottom ten cards in library order and does not reveal the hand")
    void darePreservesBottomCardsAndHandPrivacy() {
        List<Card> library = cards(15);
        List<Card> controllerLibrary = cards(12);
        harness.setLibrary(player2, library);
        harness.setLibrary(player1, controllerLibrary);
        harness.setHand(player2, List.of(new Forest()));
        castTruthOrDare();

        harness.handleListChoice(player2, DARE);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library.subList(5, 15));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrderElementsOf(library.subList(0, 5));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(controllerLibrary);
        assertThat(player1SeesOpponentHand()).isFalse();
    }

    @Test
    @DisplayName("Dare leaves libraries smaller than ten cards unchanged")
    void dareWithSmallLibrary() {
        List<Card> library = cards(4);
        harness.setLibrary(player2, library);
        castTruthOrDare();

        harness.handleListChoice(player2, DARE);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dare can be chosen with an empty library")
    void dareWithEmptyLibrary() {
        harness.setLibrary(player2, List.of());
        castTruthOrDare();

        harness.handleListChoice(player2, DARE);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Truth reveals future hand contents even when the hand was initially empty and does not mill")
    void truthWithInitiallyEmptyHand() {
        List<Card> library = cards(12);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, library);
        castTruthOrDare();

        harness.handleListChoice(player2, TRUTH);
        harness.setHand(player2, List.of(new Forest()));

        assertThat(player1SeesOpponentHand()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void castTruthOrDare() {
        harness.setHand(player1, List.of(new TruthOrDare()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }

    private boolean player1SeesOpponentHand() {
        harness.clearMessages();
        harness.publishState();
        return harness.getConn1().getSentMessages().stream()
                .anyMatch(message -> message.contains("\"opponentHand\"") && message.contains("Forest"));
    }
}
