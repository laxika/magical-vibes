package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PalaceSentinels.class})
class PalaceSentinelsTest extends BaseCardTest {

    @Test
    @DisplayName("Makes its controller the monarch when it enters")
    void makesControllerMonarchWhenItEnters() {
        castPalaceSentinels(player1);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Replaces the existing monarch when it enters")
    void replacesExistingMonarch() {
        gd.monarchPlayerId = player2.getId();

        castPalaceSentinels(player1);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The other player becomes monarch when their Sentinels enters")
    void makesOtherControllerMonarch() {
        gd.monarchPlayerId = player1.getId();

        castPalaceSentinels(player2);

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Entering while already monarch preserves the designation")
    void alreadyMonarchRemainsMonarch() {
        gd.monarchPlayerId = player1.getId();

        castPalaceSentinels(player1);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The monarch changes only when the enter trigger resolves")
    void monarchChangesOnTriggerResolution() {
        gd.monarchPlayerId = player2.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new PalaceSentinels(), "{3}{W}");

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Palace Sentinels");
        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());

        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    private void castPalaceSentinels(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, new PalaceSentinels(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
