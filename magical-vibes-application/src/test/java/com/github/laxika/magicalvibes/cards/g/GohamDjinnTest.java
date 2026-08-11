package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GohamDjinnTest extends BaseCardTest {

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

    @Test
    @DisplayName("Shrinks when black is the most common color")
    void shrinksWhenBlackIsMostCommon() {
        Permanent goham = addGohamDjinn();

        assertThat(gqs.getEffectivePower(gd, goham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shrinks when black is tied for most common color")
    void shrinksWhenBlackIsTied() {
        Permanent goham = addGohamDjinn();
        harness.addToBattlefield(player2, createCreature("Blue Creature", CardColor.BLUE));

        assertThat(gqs.getEffectivePower(gd, goham)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goham)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not shrink when another color is more common")
    void doesNotShrinkWhenAnotherColorIsMoreCommon() {
        Permanent goham = addGohamDjinn();
        harness.addToBattlefield(player2, createCreature("Blue Creature 1", CardColor.BLUE));
        harness.addToBattlefield(player2, createCreature("Blue Creature 2", CardColor.BLUE));

        assertThat(gqs.getEffectivePower(gd, goham)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, goham)).isEqualTo(5);
    }

    @Test
    @DisplayName("Activating the ability grants a regeneration shield")
    void activatingAbilityGrantsRegenerationShield() {
        Permanent goham = addGohamDjinn();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(goham.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent addGohamDjinn() {
        harness.addToBattlefield(player1, new GohamDjinn());
        return findPermanent(player1, "Goham Djinn");
    }
}
