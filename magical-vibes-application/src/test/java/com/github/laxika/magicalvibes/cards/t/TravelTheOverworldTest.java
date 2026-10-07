package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BalambGardenSeeDAcademy;
import com.github.laxika.magicalvibes.cards.b.BalambGardenAirborne;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TravelTheOverworld.class, Forest.class, BalambGardenSeeDAcademy.class, BalambGardenAirborne.class})
class TravelTheOverworldTest extends BaseCardTest {

    @Test
    @DisplayName("Town affinity lets Travel the Overworld be cast for only blue mana")
    void townAffinityReducesGenericCostAndDrawsFourCards() {
        for (int i = 0; i < 5; i++) {
            addTown(player1);
        }
        harness.setHand(player1, List.of(new TravelTheOverworld()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Travel the Overworld");
    }

    @Test
    @DisplayName("Town affinity counts only Towns controlled by the spell's controller")
    void opponentTownsDoNotReduceCost() {
        for (int i = 0; i < 5; i++) {
            addTown(player2);
        }
        harness.setHand(player1, List.of(new TravelTheOverworld()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private void addTown(Player player) {
        harness.addToBattlefield(player, new BalambGardenSeeDAcademy());
    }

    @Test
    void twoTownsReduceTheGenericCostByTwo() {
        addTown(player1);
        addTown(player1);
        harness.setHand(player1, List.of(new TravelTheOverworld()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Travel the Overworld");
    }

    @Test
    void excessTownsDoNotReduceTheBlueRequirement() {
        for (int i = 0; i < 7; i++) {
            addTown(player1);
        }
        harness.setHand(player1, List.of(new TravelTheOverworld()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void ordinaryLandsDoNotReduceTheCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new TravelTheOverworld()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
