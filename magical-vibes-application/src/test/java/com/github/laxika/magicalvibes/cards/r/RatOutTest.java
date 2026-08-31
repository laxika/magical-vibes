package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RatOut.class, GrizzlyBears.class})
class RatOutTest extends BaseCardTest {

    @Test
    @DisplayName("Rat Out gives a target creature -1/-1 and creates a Rat that can't block")
    void debuffsTargetAndCreatesNonBlockingRat() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RatOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);

        Permanent rat = findPermanents(player1, "Rat").getFirst();
        assertThat(bls.canBlock(gd, rat)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Rat Out creates a Rat when no creature is chosen")
    void createsRatWithoutTarget() {
        harness.setHand(player1, List.of(new RatOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent rat = findPermanents(player1, "Rat").getFirst();
        assertThat(bls.canBlock(gd, rat)).isFalse();
    }
}
