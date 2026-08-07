package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DrippingTongueZuberaTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 Spirit token for each Zubera that died this turn")
    void createsSpiritTokenForEachZuberaThatDiedThisTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DrippingTongueZubera());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DrippingTongueZubera());
        Permanent nonZubera = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castInstant(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(spiritTokens()).isEqualTo(1);

        harness.castInstant(player1, 0, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(spiritTokens()).isEqualTo(3);

        harness.castInstant(player1, 0, nonZubera.getId());
        harness.passBothPriorities();
        assertThat(spiritTokens()).isEqualTo(3);
    }

    private long spiritTokens() {
        return countPermanents(player2, "Spirit");
    }
}
