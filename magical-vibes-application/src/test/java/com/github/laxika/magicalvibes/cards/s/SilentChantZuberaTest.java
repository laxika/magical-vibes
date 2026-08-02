package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SilentChantZuberaTest extends BaseCardTest {

    @Test
    @DisplayName("Gains two life for each Zubera that died this turn")
    void gainsLifeForEachZuberaThatDiedThisTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SilentChantZubera());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SilentChantZubera());
        Permanent nonZubera = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castInstant(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);

        harness.castInstant(player1, 0, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);

        harness.castInstant(player1, 0, nonZubera.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
    }
}
