package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.v.VedalkenGhoul;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrasticRevelation.class, VedalkenGhoul.class})
class DrasticRevelationTest extends BaseCardTest {

    private void addCost() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Casting Drastic Revelation puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new DrasticRevelation()));
        addCost();

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new DrasticRevelation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Discards existing hand, draws seven, then discards three at random")
    void discardsHandDrawsSevenThenDiscardsThree() {
        // Two extra cards in hand alongside the spell; a fresh 7-card library to draw from.
        harness.setHand(player1, List.of(new DrasticRevelation(), new VedalkenGhoul(), new VedalkenGhoul()));
        harness.setLibrary(player1, List.of(
                new VedalkenGhoul(), new VedalkenGhoul(), new VedalkenGhoul(), new VedalkenGhoul(),
                new VedalkenGhoul(), new VedalkenGhoul(), new VedalkenGhoul()));
        addCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        // Discard hand (the 2 leftover cards), draw 7, discard 3 at random => hand of 4.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        // Library emptied by the 7-card draw.
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        // Graveyard: 2 discarded hand + 3 random discards + Drastic Revelation itself = 6.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        harness.assertInGraveyard(player1, "Drastic Revelation");
        // Random discard prompts nothing.
        assertThat(gd.interaction.activeInteraction()).isNull();
        // Exactly three "at random" discards logged.
        long randomDiscardLogs = gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("discards") && log.contains("at random"))
                .count();
        assertThat(randomDiscardLogs).isEqualTo(3);
    }

    @Test
    @DisplayName("Empty starting hand still draws seven then discards three at random")
    void emptyHandStillDrawsAndDiscards() {
        harness.setHand(player1, List.of(new DrasticRevelation()));
        harness.setLibrary(player1, List.of(
                new VedalkenGhoul(), new VedalkenGhoul(), new VedalkenGhoul(), new VedalkenGhoul(),
                new VedalkenGhoul(), new VedalkenGhoul(), new VedalkenGhoul()));
        addCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        // No leftover hand to discard; draw 7, discard 3 => hand of 4.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        // Graveyard: 3 random discards + the spell = 4.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Original hand is discarded before drawing and opponent's zones are unchanged")
    void discardsOriginalCardsBeforeDrawingAndOnlyAffectsController() {
        VedalkenGhoul original = new VedalkenGhoul();
        VedalkenGhoul opponentCard = new VedalkenGhoul();
        List<VedalkenGhoul> drawnCards = java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> new VedalkenGhoul()).toList();
        List<VedalkenGhoul> opponentLibrary = List.of(new VedalkenGhoul());
        harness.setHand(player1, List.of(new DrasticRevelation(), original));
        harness.setHand(player2, List.of(opponentCard));
        harness.setLibrary(player1, drawnCards);
        harness.setLibrary(player2, opponentLibrary);
        addCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4)
                .isSubsetOf(drawnCards).doesNotContain(original);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(original);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(drawnCards::contains).toList()).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("With fewer than three cards available, discards all drawn cards before losing")
    void shortLibraryStillDiscardsBeforeControllerLoses() {
        VedalkenGhoul first = new VedalkenGhoul();
        VedalkenGhoul second = new VedalkenGhoul();
        harness.setHand(player1, List.of(new DrasticRevelation()));
        harness.setLibrary(player1, List.of(first, second));
        addCost();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(first, second);
        harness.assertInGraveyard(player1, "Drastic Revelation");
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
