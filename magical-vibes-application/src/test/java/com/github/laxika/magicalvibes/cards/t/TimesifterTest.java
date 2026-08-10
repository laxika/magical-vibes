package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TimesifterTest extends BaseCardTest {

    private void resolveTimesifter(List<Card> player1Library, List<Card> player2Library) {
        harness.addToBattlefield(player1, new Timesifter());
        harness.setLibrary(player1, player1Library);
        harness.setLibrary(player2, player2Library);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The player exiling the card with the greatest mana value takes an extra turn")
    void greatestManaValueWins() {
        Card player1Card = new HillGiant();
        Card player2Card = new GrizzlyBears();

        resolveTimesifter(List.of(player1Card), List.of(player2Card));

        assertThat(gd.extraTurns).containsExactly(player1.getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactly(player1Card, player2Card);
    }

    @Test
    @DisplayName("Tied players repeat the comparison until the tie is broken")
    void tiedPlayersRepeat() {
        Card firstPlayer1Card = new GrizzlyBears();
        Card firstPlayer2Card = new GrizzlyBears();
        Card secondPlayer1Card = new HillGiant();
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
        Card player2Card = new GrizzlyBears();

        resolveTimesifter(List.of(), List.of(player2Card));

        assertThat(gd.extraTurns).containsExactly(player2.getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(player2Card);
    }
}
