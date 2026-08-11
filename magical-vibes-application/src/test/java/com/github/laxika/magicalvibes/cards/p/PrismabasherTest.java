package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrismabasherTest extends BaseCardTest {

    @Test
    void givesPlusOnePlusOneWithOneControlledColor() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castPrismabasher(List.of(bears.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    void givesPlusTwoPlusTwoToTwoTargetsWithTwoControlledColors() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new WindDrake());
        castPrismabasher(List.of(bears.getId(), drake.getId()));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, drake)).isEqualTo(4);
    }

    @Test
    void cannotChooseMoreTargetsThanControlledColors() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Prismabasher()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castPrismabasher(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new Prismabasher()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
