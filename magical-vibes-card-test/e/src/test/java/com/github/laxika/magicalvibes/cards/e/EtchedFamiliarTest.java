package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(EtchedFamiliar.class)
class EtchedFamiliarTest extends BaseCardTest {

    @Test
    void whenItDiesEachOpponentLosesTwoLifeAndControllerGainsTwoLife() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new EtchedFamiliar());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, familiar));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Etched Familiar");
    }

    @Test
    void opponentsFamiliarGainsLifeForItsController() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player2, new EtchedFamiliar());
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, familiar));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        harness.assertInGraveyard(player2, "Etched Familiar");
    }

    @Test
    void exileDoesNotTriggerLifeLossOrLifeGain() {
        Permanent familiar = harness.addToBattlefieldAndReturn(player1, new EtchedFamiliar());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, familiar));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(familiar);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(familiar.getCard());
    }

    @Test
    void eachFamiliarTriggersWhenBothDieTogether() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EtchedFamiliar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EtchedFamiliar());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().performSimultaneousRemovals(
                gd, List.of(first, second), () -> {
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
                }));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard(), second.getCard());
    }
}
