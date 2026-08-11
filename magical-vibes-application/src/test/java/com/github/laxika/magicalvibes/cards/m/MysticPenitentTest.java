package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MysticPenitentTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/1 without threshold")
    void baseStatsWithoutThreshold() {
        harness.addToBattlefield(player1, new MysticPenitent());

        assertStats(1, 1, false);
    }

    @Test
    @DisplayName("Gets +1/+1 and flying with seven cards in controller's graveyard")
    void thresholdAtSevenCards() {
        fillGraveyard(player1, 7);
        harness.addToBattlefield(player1, new MysticPenitent());

        assertStats(2, 2, true);
    }

    @Test
    @DisplayName("Six cards are not enough for threshold")
    void noThresholdAtSixCards() {
        fillGraveyard(player1, 6);
        harness.addToBattlefield(player1, new MysticPenitent());

        assertStats(1, 1, false);
    }

    @Test
    @DisplayName("Opponent's graveyard does not count")
    void opponentGraveyardDoesNotCount() {
        fillGraveyard(player2, 7);
        harness.addToBattlefield(player1, new MysticPenitent());

        assertStats(1, 1, false);
    }

    @Test
    @DisplayName("Loses the bonus when graveyard drops below seven cards")
    void losesBonusWhenGraveyardShrinks() {
        fillGraveyard(player1, 7);
        harness.addToBattlefield(player1, new MysticPenitent());
        Permanent penitent = findPenitent();
        assertStats(2, 2, true);

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, penitent)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, penitent)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, penitent, Keyword.FLYING)).isFalse();
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }

    private Permanent findPenitent() {
        return findPermanent(player1, "Mystic Penitent");
    }

    private void assertStats(int power, int toughness, boolean flying) {
        Permanent penitent = findPenitent();
        assertThat(gqs.getEffectivePower(gd, penitent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, penitent)).isEqualTo(toughness);
        assertThat(gqs.hasKeyword(gd, penitent, Keyword.FLYING)).isEqualTo(flying);
    }
}
