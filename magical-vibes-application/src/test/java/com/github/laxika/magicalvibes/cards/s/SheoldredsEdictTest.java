package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SheoldredsEdictTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken creature mode sacrifices one nontoken creature per opponent")
    void sacrificesNontokenCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, creatureToken("Bear Token"));

        cast(0);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(findPermanents(player2, "Bear Token")).hasSize(1);
    }

    @Test
    @DisplayName("Creature token mode sacrifices one creature token per opponent")
    void sacrificesCreatureToken() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, creatureToken("Bear Token"));

        cast(1);

        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Bear Token")).isEmpty();
    }

    @Test
    @DisplayName("Planeswalker mode sacrifices one planeswalker per opponent")
    void sacrificesPlaneswalker() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        addPlaneswalker();

        cast(2);

        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player2, "Test Planeswalker")).isEmpty();
    }

    private void cast(int mode) {
        harness.setHand(player1, List.of(new SheoldredsEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castModalInstant(player1, 0, mode, List.of());
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker() {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setManaCost("{3}{B}");
        card.setLoyalty(4);
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, card);
        permanent.setCounterCount(CounterType.LOYALTY, 4);
        return permanent;
    }

    private static Card creatureToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setToken(true);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
