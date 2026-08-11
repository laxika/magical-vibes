package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HalamDjinnTest extends BaseCardTest {

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

    private Permanent addHalamDjinn() {
        harness.addToBattlefield(player1, new HalamDjinn());
        return findPermanent(player1, "Halam Djinn");
    }

    @Test
    @DisplayName("Shrinks when red is the most common color")
    void shrinksWhenRedIsMostCommon() {
        Permanent halam = addHalamDjinn();

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shrinks when red is tied for most common color")
    void shrinksWhenRedIsTied() {
        Permanent halam = addHalamDjinn();
        harness.addToBattlefield(player2, createCreature("Blue Creature", CardColor.BLUE));

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not shrink when another color is more common")
    void doesNotShrinkWhenAnotherColorIsMoreCommon() {
        Permanent halam = addHalamDjinn();
        harness.addToBattlefield(player2, createCreature("Blue Creature 1", CardColor.BLUE));
        harness.addToBattlefield(player2, createCreature("Blue Creature 2", CardColor.BLUE));

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts each color of multicolored permanents")
    void countsEachColorOfMulticoloredPermanents() {
        Permanent halam = addHalamDjinn();
        harness.addToBattlefield(player2,
                createCreature("Blue Green Creature 1", CardColor.BLUE, CardColor.GREEN));
        harness.addToBattlefield(player2,
                createCreature("Blue Green Creature 2", CardColor.BLUE, CardColor.GREEN));

        assertThat(gqs.getEffectivePower(gd, halam)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, halam)).isEqualTo(5);
    }
}
