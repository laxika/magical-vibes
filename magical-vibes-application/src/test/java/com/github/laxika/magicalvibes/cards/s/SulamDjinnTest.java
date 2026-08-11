package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SulamDjinnTest extends BaseCardTest {

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

    private Permanent addSulamDjinn() {
        harness.addToBattlefield(player1, new SulamDjinn());
        return findPermanent(player1, "Sulam Djinn");
    }

    @Test
    @DisplayName("Shrinks when green is the most common color")
    void shrinksWhenGreenIsMostCommon() {
        Permanent sulam = addSulamDjinn();

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(4);
    }

    @Test
    @DisplayName("Shrinks when green is tied for most common color")
    void shrinksWhenGreenIsTied() {
        Permanent sulam = addSulamDjinn();
        harness.addToBattlefield(player2, createCreature("Red Creature", CardColor.RED));

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not shrink when another color is more common")
    void doesNotShrinkWhenAnotherColorIsMoreCommon() {
        Permanent sulam = addSulamDjinn();
        harness.addToBattlefield(player2, createCreature("Red Creature 1", CardColor.RED));
        harness.addToBattlefield(player2, createCreature("Red Creature 2", CardColor.RED));

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(6);
    }

    @Test
    @DisplayName("Counts each color of multicolored permanents")
    void countsEachColorOfMulticoloredPermanents() {
        Permanent sulam = addSulamDjinn();
        harness.addToBattlefield(player2,
                createCreature("Red Blue Creature 1", CardColor.RED, CardColor.BLUE));
        harness.addToBattlefield(player2,
                createCreature("Red Blue Creature 2", CardColor.RED, CardColor.BLUE));

        assertThat(gqs.getEffectivePower(gd, sulam)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sulam)).isEqualTo(6);
    }
}
