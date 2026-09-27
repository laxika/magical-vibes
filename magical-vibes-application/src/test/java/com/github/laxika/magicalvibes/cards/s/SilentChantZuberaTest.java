package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Befoul;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilentChantZubera.class, Befoul.class, LanternKami.class})
class SilentChantZuberaTest extends BaseCardTest {

    @Test
    @DisplayName("Gains two life for each Zubera that died this turn")
    void gainsLifeForEachZuberaThatDiedThisTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SilentChantZubera());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SilentChantZubera());
        Permanent nonZubera = harness.addToBattlefieldAndReturn(player2, new LanternKami());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Befoul(), new Befoul(), new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 12);

        harness.castAndResolveSorcery(player1, 0, first.getId());
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);

        harness.castAndResolveSorcery(player1, 0, second.getId());
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);

        harness.castAndResolveSorcery(player1, 0, nonZubera.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Counts Zuberas that died under either player's control")
    void countsZuberaDeathsAcrossPlayers() {
        Permanent ownZubera = harness.addToBattlefieldAndReturn(player1, new SilentChantZubera());
        Permanent opposingZubera = harness.addToBattlefieldAndReturn(player2, new SilentChantZubera());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Befoul(), new Befoul()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castAndResolveSorcery(player1, 0, ownZubera.getId());
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.castAndResolveSorcery(player1, 0, opposingZubera.getId());
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(24);
    }
}
