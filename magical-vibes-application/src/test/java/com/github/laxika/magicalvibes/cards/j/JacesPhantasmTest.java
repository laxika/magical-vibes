package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JacesPhantasm.class, Island.class})
class JacesPhantasmTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 while no graveyard is large")
    void baseStatsWithEmptyGraveyards() {
        harness.addToBattlefield(player1, new JacesPhantasm());

        assertStats(1, 1);
    }

    @Test
    @DisplayName("Still 1/1 when opponent has only nine cards in their graveyard")
    void noBoostAtNineCards() {
        harness.addToBattlefield(player1, new JacesPhantasm());
        fillGraveyard(player2, 9);

        assertStats(1, 1);
    }

    @Test
    @DisplayName("Gets +4/+4 when opponent has ten cards in their graveyard")
    void boostAtTenCards() {
        harness.addToBattlefield(player1, new JacesPhantasm());
        fillGraveyard(player2, 10);

        assertStats(5, 5);
    }

    @Test
    @DisplayName("Controller's own full graveyard does not grant the boost")
    void ownGraveyardDoesNotCount() {
        harness.addToBattlefield(player1, new JacesPhantasm());
        fillGraveyard(player1, 12);

        assertStats(1, 1);
    }

    @Test
    @DisplayName("Loses the boost when the opponent's graveyard shrinks below ten")
    void losesBoostWhenGraveyardShrinks() {
        harness.addToBattlefield(player1, new JacesPhantasm());
        fillGraveyard(player2, 10);
        assertStats(5, 5);

        gd.playerGraveyards.get(player2.getId()).removeFirst();

        assertStats(1, 1);
    }

    @Test
    @DisplayName("Gains the bonus immediately when the opponent's graveyard reaches ten")
    void gainsBoostWhileOnBattlefield() {
        harness.addToBattlefield(player1, new JacesPhantasm());
        fillGraveyard(player2, 9);
        assertStats(1, 1);

        gd.playerGraveyards.get(player2.getId()).add(new Island());

        assertStats(5, 5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Phantasm checks its own opponent and boosts only itself")
    void opposingPhantasmsCheckTheirOwnOpponents() {
        harness.addToBattlefield(player1, new JacesPhantasm());
        harness.addToBattlefield(player2, new JacesPhantasm());
        fillGraveyard(player2, 12);

        assertStats(5, 5);
        Permanent opposingPhantasm = findPermanent(player2, "Jace's Phantasm");
        assertThat(gqs.getEffectivePower(gd, opposingPhantasm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingPhantasm)).isEqualTo(1);

        fillGraveyard(player1, 10);

        assertStats(5, 5);
        assertThat(gqs.getEffectivePower(gd, opposingPhantasm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingPhantasm)).isEqualTo(5);
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Island());
        }
        harness.setGraveyard(player, cards);
    }

    private void assertStats(int power, int toughness) {
        Permanent phantasm = findPermanent(player1, "Jace's Phantasm");
        assertThat(gqs.getEffectivePower(gd, phantasm)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, phantasm)).isEqualTo(toughness);
    }
}
