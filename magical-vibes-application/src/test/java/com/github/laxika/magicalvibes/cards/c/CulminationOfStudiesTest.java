package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CulminationOfStudies.class, Forest.class, GrizzlyBears.class, LightningBolt.class, Opt.class})
class CulminationOfStudiesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles X cards and applies land, blue, and red riders independently")
    void exilesCardsAndAppliesAllRiders() {
        Card land = new Forest();
        Card blue = new Opt();
        Card red = new LightningBolt();
        Card blueAndRed = blueAndRedCard();
        Card firstDraw = new GrizzlyBears();
        Card secondDraw = new Forest();

        harness.setHand(player1, List.of(new CulminationOfStudies()));
        harness.setLibrary(player1, List.of(land, blue, red, blueAndRed, firstDraw, secondDraw));
        addManaForX(player1, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .containsExactly(land.getId(), blue.getId(), red.getId(), blueAndRed.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p ->
                        p.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Exiles only the cards available when X exceeds the library size")
    void exilesOnlyAvailableCards() {
        Card land = new Forest();
        Card blue = new Opt();

        harness.setHand(player1, List.of(new CulminationOfStudies()));
        harness.setLibrary(player1, List.of(land, blue));
        addManaForX(player1, 5);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .containsExactly(land.getId(), blue.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p ->
                        p.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void addManaForX(Player player, int x) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, x);
    }

    private Card blueAndRedCard() {
        Card card = new Card();
        card.setName("Blue and Red Test Card");
        card.setType(CardType.INSTANT);
        card.setColor(CardColor.BLUE);
        card.setColors(List.of(CardColor.BLUE, CardColor.RED));
        return card;
    }
}
