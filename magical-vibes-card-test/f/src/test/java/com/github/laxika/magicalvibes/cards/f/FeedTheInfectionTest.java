package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeedTheInfection.class})
class FeedTheInfectionTest extends BaseCardTest {

    @Test
    void drawsThreeCardsAndLosesThreeLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FeedTheInfection()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void corruptedMakesOpponentWithThreePoisonCountersLoseThreeLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new FeedTheInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void corruptedDoesNotAffectOpponentBelowThreePoisonCounters() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new FeedTheInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void corruptedAffectsOpponentAboveThresholdButNeverAddsToCastersLifeLoss() {
        gd.playerPoisonCounters.put(player1.getId(), 5);
        gd.playerPoisonCounters.put(player2.getId(), 5);
        harness.setHand(player1, List.of(new FeedTheInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void corruptedChecksPoisonCountersAtResolution() {
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new FeedTheInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void corruptedDoesNotUsePoisonThresholdFromCastingTime() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player1, List.of(new FeedTheInfection()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }
}
