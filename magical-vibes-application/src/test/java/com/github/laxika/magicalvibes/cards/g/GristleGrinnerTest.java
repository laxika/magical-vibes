package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GristleGrinnerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 whenever another creature dies")
    void getsBoostWhenCreatureDies() {
        Permanent grinner = harness.addToBattlefieldAndReturn(player1, new GristleGrinner());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, grinner)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, grinner)).isEqualTo(5);
    }

    @Test
    @DisplayName("The death boost lasts until end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent grinner = harness.addToBattlefieldAndReturn(player1, new GristleGrinner());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, victim.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, grinner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, grinner)).isEqualTo(3);
    }
}
