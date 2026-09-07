package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WormfangManta.class)
class WormfangMantaTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield makes its controller skip their next turn")
    void enteringTheBattlefieldSkipsNextTurn() {
        castManta();

        assertThat(gd.skipNextTurnCount).containsEntry(player1.getId(), 1);
    }

    @Test
    @DisplayName("Leaving the battlefield gives its controller an extra turn")
    void leavingTheBattlefieldGivesExtraTurn() {
        Permanent manta = castManta();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, manta));
        resolvePendingTrigger();

        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    private Permanent castManta() {
        harness.setHand(player1, List.of(new WormfangManta()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Wormfang Manta");
    }

    private void resolvePendingTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
