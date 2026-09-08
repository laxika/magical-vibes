package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoulderbranchGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield gains life equal to its normal power")
    void normalCastGainsLifeEqualToPower() {
        prepareMainPhase();
        harness.setHand(player1, List.of(new BoulderbranchGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        resolveCreatureAndEnterTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    @Test
    @DisplayName("Prototype entering the battlefield gains life equal to its prototype power")
    void prototypeCastGainsLifeEqualToPrototypePower() {
        prepareMainPhase();
        harness.setHand(player1, List.of(new BoulderbranchGolem()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int lifeBefore = gd.getLife(player1.getId());

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        resolveCreatureAndEnterTrigger();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void resolveCreatureAndEnterTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
