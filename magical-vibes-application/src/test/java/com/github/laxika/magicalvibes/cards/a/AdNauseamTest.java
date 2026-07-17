package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class AdNauseamTest extends BaseCardTest {

    private void castAdNauseam(List<Card> library) {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new AdNauseam()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0);
    }

    private static List<Card> bears(int count) {
        return new ArrayList<>(IntStream.range(0, count).mapToObj(i -> (Card) new GrizzlyBears()).toList());
    }

    @Test
    @DisplayName("First reveal is mandatory: puts the top card into hand and loses life equal to its mana value")
    void firstRevealMandatory() {
        castAdNauseam(bears(3));

        // Grizzly Bears has mana value 2.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        // Decline the repeat — the process ends.
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Repeating reveals another card and loses more life each time until the controller stops")
    void repeatsUntilDeclined() {
        castAdNauseam(bears(4));

        // First (mandatory) reveal already happened: life 18, one card in hand.
        harness.handleMayAbilityChosen(player1, true);  // second reveal
        harness.handleMayAbilityChosen(player1, true);  // third reveal
        harness.handleMayAbilityChosen(player1, false); // stop

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14); // 20 - 3*2
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A zero-mana-value card is put into hand without any life loss")
    void zeroManaValueCardNoLifeLoss() {
        List<Card> library = new ArrayList<>();
        library.add(new Island()); // mana value 0
        library.add(new GrizzlyBears());
        castAdNauseam(library);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The process stops automatically once the library is empty")
    void stopsWhenLibraryEmpties() {
        castAdNauseam(bears(1));

        // Only card revealed; no repeat prompt because the library is now empty.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library reveals nothing and costs no life")
    void emptyLibraryDoesNothing() {
        castAdNauseam(new ArrayList<>());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
