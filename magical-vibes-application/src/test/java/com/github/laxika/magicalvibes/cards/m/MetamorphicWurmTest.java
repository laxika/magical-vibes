package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetamorphicWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Remains 3/3 with fewer than seven cards in its controller's graveyard")
    void noBoostBelowThreshold() {
        harness.setGraveyard(player1, createGraveyard(6));
        harness.addToBattlefield(player1, new MetamorphicWurm());

        assertStats(3, 3);
    }

    @Test
    @DisplayName("Gets +4/+4 with seven cards in its controller's graveyard")
    void getsBoostAtThreshold() {
        harness.setGraveyard(player1, createGraveyard(7));
        harness.addToBattlefield(player1, new MetamorphicWurm());

        assertStats(7, 7);
    }

    @Test
    @DisplayName("Cards in an opponent's graveyard do not enable the boost")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, createGraveyard(7));
        harness.addToBattlefield(player1, new MetamorphicWurm());

        assertStats(3, 3);
    }

    @Test
    @DisplayName("Gains and loses the boost as its controller's graveyard crosses the threshold")
    void boostUpdatesWhenGraveyardChanges() {
        harness.setGraveyard(player1, createGraveyard(6));
        harness.addToBattlefield(player1, new MetamorphicWurm());
        assertStats(3, 3);

        harness.setGraveyard(player1, createGraveyard(7));
        assertStats(7, 7);

        harness.setGraveyard(player1, createGraveyard(6));
        assertStats(3, 3);
    }

    private void assertStats(int power, int toughness) {
        Permanent wurm = findPermanent(player1, "Metamorphic Wurm");
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(toughness);
    }

    private List<Card> createGraveyard(int count) {
        List<Card> graveyard = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            graveyard.add(new MetamorphicWurm());
        }
        return graveyard;
    }
}
