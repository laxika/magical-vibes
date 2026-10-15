package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingLiesOfLoki.class})
class LivingLiesOfLokiTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 for each other Illusion you control")
    void countsOtherIllusionsYouControl() {
        Permanent loki = addCreatureReady(player1, new LivingLiesOfLoki());
        harness.addToBattlefield(player1, new LivingLiesOfLoki());
        harness.addToBattlefield(player2, new LivingLiesOfLoki());

        assertThat(gqs.getEffectivePower(gd, loki)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, loki)).isEqualTo(3);
    }

    @Test
    @DisplayName("Updates when a counted Illusion leaves")
    void updatesWhenIllusionLeaves() {
        Permanent loki = addCreatureReady(player1, new LivingLiesOfLoki());
        Permanent illusion = harness.addToBattlefieldAndReturn(player1, new LivingLiesOfLoki());

        assertThat(gqs.getEffectivePower(gd, loki)).isEqualTo(2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, illusion));

        assertThat(gqs.getEffectivePower(gd, loki)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card when it dies")
    void drawsCardWhenItDies() {
        LivingLiesOfLoki drawnCard = new LivingLiesOfLoki();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent loki = harness.addToBattlefieldAndReturn(player1, new LivingLiesOfLoki());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, loki));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Counts multiple other Illusions and excludes itself")
    void countsMultipleOtherIllusions() {
        Permanent loki = harness.addToBattlefieldAndReturn(player1, new LivingLiesOfLoki());

        assertThat(gqs.getEffectivePower(gd, loki)).isEqualTo(1);
        harness.addToBattlefield(player1, new LivingLiesOfLoki());
        harness.addToBattlefield(player1, new LivingLiesOfLoki());

        assertThat(gqs.getEffectivePower(gd, loki)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, loki)).isEqualTo(3);
    }

    @Test
    @DisplayName("Death trigger draws for the opponent when they control it")
    void drawsForOpponentController() {
        LivingLiesOfLoki drawnCard = new LivingLiesOfLoki();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard));
        Permanent loki = harness.addToBattlefieldAndReturn(player2, new LivingLiesOfLoki());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, loki));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiling it does not trigger a draw")
    void exileDoesNotDraw() {
        LivingLiesOfLoki drawnCard = new LivingLiesOfLoki();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        Permanent loki = harness.addToBattlefieldAndReturn(player1, new LivingLiesOfLoki());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, loki));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
    }
}
