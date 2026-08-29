package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeadeyeBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("With the city's blessing, combat damage draws a card")
    void drawsWithCityBlessing() {
        gd.playersWithCityBlessing.add(player1.getId());
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        Permanent brawler = addCreatureReady(player1, new DeadeyeBrawler());
        brawler.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Without the city's blessing, combat damage does not draw a card")
    void doesNotDrawWithoutCityBlessing() {
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        Permanent brawler = addCreatureReady(player1, new DeadeyeBrawler());
        brawler.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Entering as the tenth permanent grants the city's blessing")
    void enteringAsTenthPermanentGrantsCityBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new DeadeyeBrawler()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }
}
