package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mathemagics.class, AlmsCollector.class})
class MathemagicsTest extends BaseCardTest {

    @Test
    @DisplayName("The caster can target themselves and draw two cards with X=1")
    void canTargetCaster() {
        harness.setHand(player1, List.of(new Mathemagics()));
        harness.setLibrary(player1, List.of(new Mathemagics(), new Mathemagics(), new Mathemagics()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 1, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Mathemagics");
    }

    @Test
    @DisplayName("With X=2, target player draws 4 cards")
    void drawsTwoToTheXWithXTwo() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Mathemagics()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 4);
    }

    @Test
    @DisplayName("With X=0, target player draws 1 card")
    void drawsOneWithXZero() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        harness.setHand(player1, List.of(new Mathemagics()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 1);
    }

    @Test
    void targetLosesWhenUnableToDrawAllCards() {
        harness.setHand(player1, List.of(new Mathemagics()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Mathemagics()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void collectorReplacesEntireMultiCardDraw() {
        harness.addToBattlefield(player1, new AlmsCollector());
        harness.setHand(player1, List.of(new Mathemagics()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Mathemagics(), new Mathemagics()));
        harness.setLibrary(player2, List.of(new Mathemagics(), new Mathemagics(),
                new Mathemagics(), new Mathemagics(), new Mathemagics()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
    }
}
