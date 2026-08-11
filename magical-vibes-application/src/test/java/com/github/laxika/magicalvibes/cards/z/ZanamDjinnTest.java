package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZanamDjinnTest extends BaseCardTest {

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

    private Permanent addZanamDjinn() {
        harness.addToBattlefield(player1, new ZanamDjinn());
        return findPermanent(player1, "Zanam Djinn");
    }

    @Test
    @DisplayName("Shrinks when blue is the most common color")
    void shrinksWhenBlueIsMostCommon() {
        Permanent zanam = addZanamDjinn();

        assertThat(gqs.getEffectivePower(gd, zanam)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zanam)).isEqualTo(4);
    }

    @Test
    @DisplayName("Shrinks when blue is tied for most common color")
    void shrinksWhenBlueIsTied() {
        Permanent zanam = addZanamDjinn();
        harness.addToBattlefield(player2, createCreature("Red Creature", CardColor.RED));

        assertThat(gqs.getEffectivePower(gd, zanam)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zanam)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not shrink when another color is more common")
    void doesNotShrinkWhenAnotherColorIsMoreCommon() {
        Permanent zanam = addZanamDjinn();
        harness.addToBattlefield(player2, createCreature("Red Creature 1", CardColor.RED));
        harness.addToBattlefield(player2, createCreature("Red Creature 2", CardColor.RED));

        assertThat(gqs.getEffectivePower(gd, zanam)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zanam)).isEqualTo(6);
    }

    @Test
    @DisplayName("Counts each color of multicolored permanents")
    void countsEachColorOfMulticoloredPermanents() {
        Permanent zanam = addZanamDjinn();
        harness.addToBattlefield(player2,
                createCreature("Red Green Creature 1", CardColor.RED, CardColor.GREEN));
        harness.addToBattlefield(player2,
                createCreature("Red Green Creature 2", CardColor.RED, CardColor.GREEN));

        assertThat(gqs.getEffectivePower(gd, zanam)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, zanam)).isEqualTo(6);
    }
}
