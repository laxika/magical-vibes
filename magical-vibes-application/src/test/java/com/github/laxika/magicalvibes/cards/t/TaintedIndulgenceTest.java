package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        TaintedIndulgence.class, Forest.class, Shock.class, GrizzlyBears.class,
        Cancel.class, WrathOfGod.class, Divination.class, Murder.class
})
class TaintedIndulgenceTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then discards one without five distinct graveyard mana values")
    void drawsThenDiscardsWithoutThreshold() {
        Card firstDraw = new Divination();
        Card secondDraw = new Murder();
        castWithGraveyard(List.of(new Forest(), new Shock(), new GrizzlyBears(), new Cancel()),
                firstDraw, secondDraw);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card == firstDraw || card == secondDraw).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card == firstDraw || card == secondDraw).hasSize(1);
    }

    @Test
    @DisplayName("Does not discard when the graveyard has five distinct mana values")
    void skipsDiscardAtThreshold() {
        Card firstDraw = new Divination();
        Card secondDraw = new Murder();
        castWithGraveyard(List.of(
                new Forest(), new Shock(), new GrizzlyBears(), new Cancel(), new WrathOfGod()),
                firstDraw, secondDraw);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(firstDraw, secondDraw);
    }

    private void castWithGraveyard(List<Card> graveyard, Card... library) {
        harness.setGraveyard(player1, graveyard);
        harness.setLibrary(player1, List.of(library));
        harness.setHand(player1, List.of(new TaintedIndulgence()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
