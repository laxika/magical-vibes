package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SardianCliffstomperTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+0 during its controller's turn with four or more Mountains")
    void getsPowerBoostFromMountainsOnControllerTurn() {
        Permanent cliffstomper = addCliffstomperWithMountains(4);

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, cliffstomper)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cliffstomper)).isEqualTo(4);

        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, cliffstomper)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cliffstomper)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get the boost with fewer than four Mountains")
    void noBoostWithFewerThanFourMountains() {
        Permanent cliffstomper = addCliffstomperWithMountains(3);

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, cliffstomper)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, cliffstomper)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get the boost during an opponent's turn")
    void noBoostOnOpponentTurn() {
        Permanent cliffstomper = addCliffstomperWithMountains(4);

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, cliffstomper)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, cliffstomper)).isEqualTo(4);
    }

    private Permanent addCliffstomperWithMountains(int mountainCount) {
        Permanent cliffstomper = harness.addToBattlefieldAndReturn(player1, new SardianCliffstomper());
        for (int i = 0; i < mountainCount; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        return cliffstomper;
    }
}
