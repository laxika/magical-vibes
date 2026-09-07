package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

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
}
