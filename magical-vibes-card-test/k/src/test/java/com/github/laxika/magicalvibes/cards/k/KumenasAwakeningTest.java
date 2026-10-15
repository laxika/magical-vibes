package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KumenasAwakening.class, Forest.class})
class KumenasAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("On your upkeep, each player draws a card without the city's blessing")
    void eachPlayerDrawsWithoutCityBlessing() {
        harness.addToBattlefield(player1, new KumenasAwakening());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        beginUpkeep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize + 1);
    }

    @Test
    @DisplayName("With the city's blessing, only you draw a card on your upkeep")
    void onlyControllerDrawsWithCityBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new KumenasAwakening()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());

        beginUpkeep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize);
    }

    @Test
    @DisplayName("Kumena's Awakening does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new KumenasAwakening());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        int player1HandSize = gd.playerHands.get(player1.getId()).size();
        int player2HandSize = gd.playerHands.get(player2.getId()).size();

        beginUpkeep(player2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(player1HandSize);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(player2HandSize);
    }

    @Test
    @DisplayName("Gaining the city's blessing in response changes who draws")
    void checksCityBlessingAtResolution() {
        harness.addToBattlefield(player1, new KumenasAwakening());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The city's blessing remains after falling below ten permanents")
    void blessingPersistsBelowTenPermanents() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        var awakening = harness.enterBattlefieldAndReturn(player1, new KumenasAwakening());
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent != awakening);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        beginUpkeep(player1);

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's city's blessing does not replace the controller's shared draw")
    void opponentsBlessingDoesNotChangeDraw() {
        harness.addToBattlefield(player1, new KumenasAwakening());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        harness.enterBattlefieldAndReturn(player2, new KumenasAwakening());
        assertThat(gd.playersWithCityBlessing).contains(player2.getId()).doesNotContain(player1.getId());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        beginUpkeep(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private void beginUpkeep(Player player) {
        advanceToUpkeep(player);
        harness.passBothPriorities();
    }
}
