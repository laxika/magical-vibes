package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TravelTheOverworld.class, Forest.class})
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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
        Permanent town = harness.addToBattlefieldAndReturn(player, new Forest());
        TestCards.mutableCard(town).setSubtypes(List.of(CardSubtype.TOWN));
    }
}
