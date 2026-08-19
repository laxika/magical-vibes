package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecretsOfTheGoldenCityTest extends BaseCardTest {

    @Test
    @DisplayName("Without the city's blessing, draws two cards")
    void drawsTwoCardsWithoutBlessing() {
        harness.setHand(player1, List.of(new SecretsOfTheGoldenCity()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addMana();

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("With the city's blessing, draws three cards")
    void drawsThreeCardsWithBlessing() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new SecretsOfTheGoldenCity()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addMana();

        castAndResolve();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castAndResolve() {
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
