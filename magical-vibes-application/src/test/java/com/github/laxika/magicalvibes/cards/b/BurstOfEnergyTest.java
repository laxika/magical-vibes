package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BurstOfEnergyTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target permanent")
    void untapsTargetPermanent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new BurstOfEnergy()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }
}
