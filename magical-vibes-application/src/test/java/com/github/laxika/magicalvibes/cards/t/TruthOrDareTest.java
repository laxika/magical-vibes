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

    private void castTruthOrDare() {
        harness.setHand(player1, List.of(new TruthOrDare()));
        addMana();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
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
