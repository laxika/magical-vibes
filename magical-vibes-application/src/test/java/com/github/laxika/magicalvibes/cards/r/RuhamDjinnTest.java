package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuhamDjinnTest extends BaseCardTest {

    private static Card createCreature(String name, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setColor(colors[0]);
        card.setColors(List.of(colors));
        return card;
    }

    private Permanent addRuhamDjinn() {
        harness.addToBattlefield(player1, new RuhamDjinn());
        return findPermanent(player1, "Ruham Djinn");
    }

    @Test
    @DisplayName("Shrinks when white is the most common color")
    void shrinksWhenWhiteIsMostCommon() {
        Permanent ruham = addRuhamDjinn();

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shrinks when white is tied for most common color")
    void shrinksWhenWhiteIsTied() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2, createCreature("Blue Creature", CardColor.BLUE));

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not shrink when another color is more common")
    void doesNotShrinkWhenAnotherColorIsMoreCommon() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2, createCreature("Blue Creature 1", CardColor.BLUE));
        harness.addToBattlefield(player2, createCreature("Blue Creature 2", CardColor.BLUE));

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts each color of multicolored permanents")
    void countsEachColorOfMulticoloredPermanents() {
        Permanent ruham = addRuhamDjinn();
        harness.addToBattlefield(player2,
                createCreature("Blue Red Creature 1", CardColor.BLUE, CardColor.RED));
        harness.addToBattlefield(player2,
                createCreature("Blue Red Creature 2", CardColor.BLUE, CardColor.RED));

        assertThat(gqs.getEffectivePower(gd, ruham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ruham)).isEqualTo(5);
    }
}
