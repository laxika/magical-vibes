package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApproachOfTheSecondSunTest extends BaseCardTest {

    @Test
    @DisplayName("First cast: gains 7 life and goes seventh from the top of the library")
    void firstCastGainsLifeAndTucksSeventhFromTop() {
        ApproachOfTheSecondSun approach = new ApproachOfTheSecondSun();
        harness.setHand(player1, List.of(approach));
        harness.addMana(player1, ManaColor.WHITE, 7);
        // Six filler cards so "seventh from the top" is a real position.
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        int lifeBefore = gd.getLife(player1.getId());
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 7);

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(7);
        assertThat(library.get(6)).isSameAs(approach);
    }

    @Test
    @DisplayName("Wins the game when casting a second same-named spell from hand this game")
    void secondCastFromHandWinsGame() {
        // A prior Approach was cast earlier this game.
        gd.recordSpellCast(player1.getId(), new ApproachOfTheSecondSun());

        harness.setHand(player1, List.of(new ApproachOfTheSecondSun()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("A single Approach cast this game does not win even from hand")
    void singleCastDoesNotWin() {
        harness.setHand(player1, List.of(new ApproachOfTheSecondSun()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
