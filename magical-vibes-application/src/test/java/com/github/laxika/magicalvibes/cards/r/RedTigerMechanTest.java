package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedTigerMechan.class})
class RedTigerMechanTest extends BaseCardTest {

    @Test
    @DisplayName("Warp casts Red Tiger Mechan for {1}{R} and allows it to attack immediately")
    void warpCastHasteAllowsImmediateAttack() {
        RedTigerMechan mechan = new RedTigerMechan();
        harness.setHand(player1, List.of(mechan));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Warp exiles Red Tiger Mechan at the next end step")
    void warpExilesAtNextEndStep() {
        RedTigerMechan mechan = new RedTigerMechan();
        harness.setHand(player1, List.of(mechan));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(mechan.getId())).isNotNull();
    }
}
