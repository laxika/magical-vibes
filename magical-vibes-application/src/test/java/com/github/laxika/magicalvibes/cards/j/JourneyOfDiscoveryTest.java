package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JourneyOfDiscovery.class, Forest.class, Island.class, YotianSoldier.class})
class JourneyOfDiscoveryTest extends BaseCardTest {

    @Test
    @DisplayName("Search mode puts up to two basic lands into your hand")
    void searchesForBasicLands() {
        cast(new int[]{0}, false, List.of(new JourneyOfDiscovery()),
                List.of(new Forest(), new Island(), new YotianSoldier()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Yotian Soldier");
    }

    @Test
    @DisplayName("Search mode can choose only one available basic land")
    void searchesForOneAvailableBasicLand() {
        Forest forest = new Forest();
        cast(new int[]{0}, false, List.of(new JourneyOfDiscovery()), List.of(forest));

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Search mode may find no basic lands")
    void mayFindNoBasicLands() {
        Forest forest = new Forest();
        cast(new int[]{0}, false, List.of(new JourneyOfDiscovery()), List.of(forest));

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Land mode allows three land plays this turn")
    void allowsTwoAdditionalLandPlays() {
        harness.setHand(player1, List.of(new JourneyOfDiscovery(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Entwine resolves both modes and pays the additional two generic and green mana")
    void entwined() {
        cast(new int[]{0, 1}, true,
                List.of(new JourneyOfDiscovery(), new Forest(), new Forest(), new Forest()),
                List.of(new Forest(), new Island()));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Entwine without its additional mana is rejected")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new JourneyOfDiscovery()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, boolean entwined, List<Card> hand, List<Card> library) {
        harness.setHand(player1, hand);
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, entwined ? 2 : 1);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 4 : 2);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, List.of(), null);
        harness.passBothPriorities();
    }

}
