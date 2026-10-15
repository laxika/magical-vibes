package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MindSpring.class)
class MindSpringTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=3: {3}{U}{U} = 5

        harness.castSorcery(player1, 0, 3);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    // ===== Resolution: draw cards =====

    @Test
    @DisplayName("X=3 draws 3 cards for the controller")
    void xEqualsThreeDrawsThreeCards() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 5); // X=3: {3}{U}{U} = 5
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1; // -1 for the spell leaving hand

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("X=0 draws no cards")
    void xZeroDrawsNoCards() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 2); // X=0: {0}{U}{U} = 2
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1; // -1 for the spell leaving hand

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("X=1 draws 1 card")
    void xOneDrawsOneCard() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 3); // X=1: {1}{U}{U} = 3
        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1; // -1 for the spell leaving hand

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    // ===== Graveyard and stack cleanup =====

    @Test
    @DisplayName("Mind Spring goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=2

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertInGraveyard(player1, "Mind Spring");
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=2

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.stack).isEmpty();
    }
    @Test
    @DisplayName("Drawing exactly the remaining library only draws for the controller without losing")
    void drawsExactlyRemainingLibraryOnlyForController() {
        MindSpring first = new MindSpring();
        MindSpring second = new MindSpring();
        MindSpring opponentCard = new MindSpring();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new MindSpring()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("X=0 with an empty library resolves without losing the game")
    void zeroWithEmptyLibraryDoesNotLose() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player1, "Mind Spring");
        assertThat(gd.stack).isEmpty();
    }
}
