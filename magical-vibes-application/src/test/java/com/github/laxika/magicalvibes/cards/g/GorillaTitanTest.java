package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GorillaTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +4/+4 with an empty graveyard")
    void getsBoostWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new GorillaTitan());

        assertStats(8, 8);
    }

    @Test
    @DisplayName("Loses the boost when a card enters its controller's graveyard")
    void losesBoostWhenGraveyardIsNotEmpty() {
        harness.addToBattlefield(player1, new GorillaTitan());
        assertStats(8, 8);

        harness.setGraveyard(player1, List.of(new GorillaTitan()));

        assertStats(4, 4);
    }

    @Test
    @DisplayName("Gets the boost again when its controller's graveyard becomes empty")
    void regainsBoostWhenGraveyardBecomesEmpty() {
        harness.setGraveyard(player1, List.of(new GorillaTitan()));
        harness.addToBattlefield(player1, new GorillaTitan());
        assertStats(4, 4);

        harness.setGraveyard(player1, List.of());

        assertStats(8, 8);
    }

    @Test
    @DisplayName("Opponent's graveyard does not disable the boost")
    void opponentGraveyardDoesNotMatter() {
        harness.setGraveyard(player2, List.of(new GorillaTitan()));
        harness.addToBattlefield(player1, new GorillaTitan());

        assertStats(8, 8);
    }

    private void assertStats(int power, int toughness) {
        Permanent titan = findPermanent(player1, "Gorilla Titan");
        assertThat(gqs.getEffectivePower(gd, titan)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, titan)).isEqualTo(toughness);
    }
}
