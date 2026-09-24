package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PalaceSentinels.class)
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

    private void castPalaceSentinels(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new PalaceSentinels()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
