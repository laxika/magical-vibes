package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CratersClawsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage without ferocious")
    void dealsXDamageWithoutFerocious() {
        castCratersClaws(3);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals X plus 2 damage with ferocious")
    void dealsXPlusTwoDamageWithFerocious() {
        addCreatureReady(player1, makeCreature("Ferocious Creature", 4, 4));
        castCratersClaws(3);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    private void castCratersClaws(int xValue) {
        harness.setHand(player1, List.of(new CratersClaws()));
        harness.addMana(player1, ManaColor.RED, xValue + 1);
        harness.castSorcery(player1, 0, xValue, player2.getId());
        harness.passBothPriorities();
    }

    private Card makeCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
