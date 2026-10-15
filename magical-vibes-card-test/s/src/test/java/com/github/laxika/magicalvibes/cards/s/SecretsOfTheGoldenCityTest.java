package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SecretsOfTheGoldenCity.class, Forest.class})
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

    @Test
    @DisplayName("Nine permanents do not count the sorcery on the stack toward ascend")
    void ninePermanentsDoNotGrantBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new SecretsOfTheGoldenCity()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addMana();

        castAndResolve();

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Ascend checks the permanent count on resolution, not casting")
    void gainsBlessingWhenTenthPermanentArrivesBeforeResolution() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new SecretsOfTheGoldenCity()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addMana();

        harness.castSorcery(player1, 0);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Losing the tenth permanent before resolution prevents the blessing")
    void doesNotGainBlessingAtCasting() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new SecretsOfTheGoldenCity()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addMana();

        harness.castSorcery(player1, 0);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        gd.playerBattlefields.get(player1.getId()).remove(0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A previously acquired blessing still draws three with no permanents")
    void blessingPersistsAfterLosingPermanents() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new SecretsOfTheGoldenCity()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addMana();
        castAndResolve();
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new SecretsOfTheGoldenCity()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addMana();
        castAndResolve();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's permanents and blessing do not upgrade the draw")
    void opponentsBlessingDoesNotApplyToController() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        gd.playersWithCityBlessing.add(player2.getId());
        harness.setHand(player1, List.of(new SecretsOfTheGoldenCity()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        addMana();

        castAndResolve();

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castAndResolve() {
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
