package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ostracize.class, GrizzlyBears.class, GiantGrowth.class})
class OstracizeTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature card discards it to opponent's graveyard")
    void choosingCreatureDiscardsIt() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new GiantGrowth()));
        harness.setHand(player1, List.of(new Ostracize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Resolving reveals the target opponent's hand to both players")
    void resolvingRevealsHandToBothPlayers() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new GiantGrowth()));
        harness.setHand(player1, List.of(new Ostracize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.clearMessages();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message -> message.contains("REVEAL_HAND"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message -> message.contains("REVEAL_HAND"));
    }

    @Test
    @DisplayName("Only creature cards are valid choices")
    void onlyCreaturesAreValid() {
        harness.setHand(player2, List.of(new GiantGrowth(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Ostracize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        // Only index 1 (Grizzly Bears) is a creature
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Selecting a noncreature card is rejected")
    void selectingNoncreatureRejected() {
        harness.setHand(player2, List.of(new GiantGrowth(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Ostracize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Hand with no creatures results in no valid choices")
    void handWithNoCreaturesNoValidChoices() {
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.setHand(player1, List.of(new Ostracize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Wrong player cannot choose")
    void wrongPlayerCannotChoose() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Ostracize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Ostracize()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
