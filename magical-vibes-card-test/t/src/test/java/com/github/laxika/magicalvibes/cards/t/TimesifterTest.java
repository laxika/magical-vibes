package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MyrEnforcer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Timesifter.class, Forest.class, AlphaMyr.class, MyrEnforcer.class})
class TimesifterTest extends BaseCardTest {

    private void resolveTimesifter(List<Card> player1Library, List<Card> player2Library) {
        resolveTimesifter(player1, player1Library, player2Library);
    }

    private void resolveTimesifter(
            Player activePlayer,
            List<Card> player1Library,
            List<Card> player2Library) {
        harness.addToBattlefield(player1, new Timesifter());
        harness.setLibrary(player1, player1Library);
        harness.setLibrary(player2, player2Library);
        advanceToUpkeep(activePlayer);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The player exiling the card with the greatest mana value takes an extra turn")
    void greatestManaValueWins() {
        Card player1Card = new MyrEnforcer();
        Card player2Card = new AlphaMyr();

        resolveTimesifter(List.of(player1Card), List.of(player2Card));

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactly(player1Card, player2Card);
    }

    @Test
    @DisplayName("Tied players repeat the comparison until the tie is broken")
    void tiedPlayersRepeat() {
        Card firstPlayer1Card = new AlphaMyr();
        Card firstPlayer2Card = new AlphaMyr();
        Card secondPlayer1Card = new MyrEnforcer();
        Card secondPlayer2Card = new Forest();

        resolveTimesifter(
                List.of(firstPlayer1Card, secondPlayer1Card),
                List.of(firstPlayer2Card, secondPlayer2Card));

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactly(firstPlayer1Card, firstPlayer2Card, secondPlayer1Card, secondPlayer2Card);
    }

    @Test
    @DisplayName("A player with an empty library cannot win the comparison")
    void emptyLibraryPlayerCannotWin() {
        Card player2Card = new AlphaMyr();

        resolveTimesifter(List.of(), List.of(player2Card));

        assertThat(gd.extraTurns).containsExactly(player2.getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(player2Card);
    }

    @Test
    @DisplayName("A player who runs out of cards during a tie cannot win the repeat")
    void tiedPlayerWithEmptyLibraryCannotWinOnRepeat() {
        Card firstPlayer1Card = new AlphaMyr();
        Card firstPlayer2Card = new AlphaMyr();
        Card secondPlayer2Card = new MyrEnforcer();

        resolveTimesifter(List.of(firstPlayer1Card), List.of(firstPlayer2Card, secondPlayer2Card));

        assertThat(gd.extraTurns).containsExactly(player2.getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactly(firstPlayer1Card, firstPlayer2Card, secondPlayer2Card);
    }

    @Test
    @DisplayName("No player takes an extra turn when no player can exile a card")
    void noPlayerWinsWhenLibrariesAreEmpty() {
        resolveTimesifter(List.of(), List.of());

        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("The ability triggers during the opponent's upkeep as well")
    void triggersDuringOpponentsUpkeep() {
        Card player1Card = new MyrEnforcer();
        Card player2Card = new AlphaMyr();

        resolveTimesifter(player2, List.of(player1Card), List.of(player2Card));

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactly(player1Card, player2Card);
    }

    @Test
    @DisplayName("A land wins against a player who cannot exile a card")
    void zeroManaValueWinsAgainstEmptyLibrary() {
        Card land = new Forest();

        resolveTimesifter(List.of(), List.of(land));

        assertThat(gd.extraTurns).containsExactly(player2.getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(land);
    }

    @Test
    @DisplayName("No extra turn is awarded when both tied players exhaust their libraries")
    void tiedPlayersBothExhaustLibraries() {
        Card player1Card = new AlphaMyr();
        Card player2Card = new AlphaMyr();

        resolveTimesifter(List.of(player1Card), List.of(player2Card));

        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactly(player1Card, player2Card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Multiple Timesifters resolve independently and create extra turns in reverse order")
    void multipleTimesiftersCreateIndependentExtraTurns() {
        Card firstPlayer1Card = new MyrEnforcer();
        Card firstPlayer2Card = new AlphaMyr();
        Card secondPlayer1Card = new Forest();
        Card secondPlayer2Card = new MyrEnforcer();
        harness.addToBattlefield(player1, new Timesifter());
        harness.addToBattlefield(player2, new Timesifter());
        harness.setLibrary(player1, List.of(firstPlayer1Card, secondPlayer1Card));
        harness.setLibrary(player2, List.of(firstPlayer2Card, secondPlayer2Card));

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.extraTurns).containsExactly(player2.getId(), player1.getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactly(firstPlayer1Card, firstPlayer2Card, secondPlayer1Card, secondPlayer2Card);
    }
}
