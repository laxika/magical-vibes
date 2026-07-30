package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WildwoodGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 during its controller's turn")
    void boostedOnControllerTurn() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new WildwoodGeist());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(5);
    }

    @Test
    @DisplayName("Is a plain 3/3 during other players' turns")
    void notBoostedOnOpponentTurn() {
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new WildwoodGeist());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost follows the controller, not the card owner's opponent")
    void boostFlipsWithActivePlayer() {
        Permanent ownGeist = harness.addToBattlefieldAndReturn(player1, new WildwoodGeist());
        Permanent enemyGeist = harness.addToBattlefieldAndReturn(player2, new WildwoodGeist());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, ownGeist)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, enemyGeist)).isEqualTo(3);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, ownGeist)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, enemyGeist)).isEqualTo(5);
    }
}
