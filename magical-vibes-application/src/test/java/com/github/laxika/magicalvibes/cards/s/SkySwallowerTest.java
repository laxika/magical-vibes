package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkySwallower.class, GrizzlyBears.class})
class SkySwallowerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives the target opponent control of all other permanents you control")
    void givesOpponentControlOfAllOtherPermanents() {
        Permanent bearA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bearB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SkySwallower()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bearA.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bearB.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Sky Swallower"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opponentBear)
                .contains(bearA, bearB);
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new SkySwallower()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }
}
