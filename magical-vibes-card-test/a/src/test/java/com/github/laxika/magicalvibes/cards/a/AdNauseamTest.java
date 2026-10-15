package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SigilOfDistinction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdNauseam.class, DruidOfTheAnima.class, Island.class, SigilOfDistinction.class})
class AdNauseamTest extends BaseCardTest {

    private void castAdNauseam(List<Card> library) {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new AdNauseam(), "{3}{B}{B}");
        harness.passBothPriorities();
    }

    private static List<Card> druids(int count) {
        return new ArrayList<>(IntStream.range(0, count).mapToObj(i -> (Card) new DruidOfTheAnima()).toList());
    }

    @Test
    @DisplayName("First reveal is mandatory: puts the top card into hand and loses life equal to its mana value")
    void firstRevealMandatory() {
        castAdNauseam(druids(3));

        // Druid of the Anima has mana value 2.
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
        castAdNauseam(druids(4));

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
        library.add(new DruidOfTheAnima());
        castAdNauseam(library);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The process stops automatically once the library is empty")
    void stopsWhenLibraryEmpties() {
        castAdNauseam(druids(1));

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

    @Test
    @DisplayName("Each iteration uses the next card's mana value, with X counting as zero")
    void revealsDifferentManaValuesInLibraryOrder() {
        List<Card> opponentHand = List.copyOf(gd.playerHands.get(player2.getId()));
        Card sigil = new SigilOfDistinction();
        Card druid = new DruidOfTheAnima();
        Card adNauseam = new AdNauseam();
        Card island = new Island();
        castAdNauseam(List.of(sigil, druid, adNauseam, island));

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sigil);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(druid, adNauseam, island);

        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sigil, druid);

        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 13);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sigil, druid, adNauseam);

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyElementsOf(opponentHand);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Reveals can continue at zero or negative life until the spell finishes resolving")
    void continuesRevealingAtNonpositiveLife() {
        castAdNauseam(druids(12));

        for (int i = 0; i < 9; i++) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertLife(player1, 0);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, -2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(11);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
