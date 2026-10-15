package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColdWaterSnapper.class, BlinkOfAnEye.class, IcyManipulator.class})
class ColdWaterSnapperTest extends BaseCardTest {

    @Test
    void opponentCannotTargetWithSpell() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new ColdWaterSnapper());
        harness.setHand(player2, List.of(new BlinkOfAnEye()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, snapper.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player1, "Cold-Water Snapper");
    }

    @Test
    void controllerCanTargetWithSpell() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new ColdWaterSnapper());
        harness.setHand(player1, List.of(new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, snapper.getId());

        harness.assertNotOnBattlefield(player1, "Cold-Water Snapper");
        harness.assertInHand(player1, "Cold-Water Snapper");
    }

    @Test
    void opponentCannotTargetWithActivatedAbility() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new ColdWaterSnapper());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, snapper.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(snapper.isTapped()).isFalse();
    }

    @Test
    void controllerCanTargetWithActivatedAbility() {
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new ColdWaterSnapper());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, snapper.getId());
        harness.passBothPriorities();

        assertThat(snapper.isTapped()).isTrue();
    }
}
