package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TwilightProphetTest extends BaseCardTest {

    @Test
    @DisplayName("With the city's blessing, reveals the top card, drains opponents, and puts it into hand")
    void blessingRevealsAndDrains() {
        harness.addToBattlefield(player1, new TwilightProphet());
        gd.playersWithCityBlessing.add(player1.getId());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        beginUpkeep(player1);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Without the city's blessing, the upkeep ability does not resolve")
    void noBlessingDoesNotTriggerEffect() {
        harness.addToBattlefield(player1, new TwilightProphet());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        beginUpkeep(player1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An empty library causes no life change")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new TwilightProphet());
        gd.playersWithCityBlessing.add(player1.getId());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        beginUpkeep(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void beginUpkeep(Player player) {
        advanceToUpkeep(player);
        harness.passBothPriorities();
    }
}
