package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CrimsonFleetCommodore.class)
class CrimsonFleetCommodoreTest extends BaseCardTest {

    @Test
    @DisplayName("Makes its controller the monarch when it enters")
    void makesControllerMonarchWhenItEnters() {
        castCrimsonFleetCommodore(player1);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Replaces the existing monarch when it enters")
    void replacesExistingMonarch() {
        gd.monarchPlayerId = player2.getId();

        castCrimsonFleetCommodore(player1);

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    private void castCrimsonFleetCommodore(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new CrimsonFleetCommodore()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
