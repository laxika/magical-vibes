package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BorrowedMalevolenceTest extends BaseCardTest {

    @Test
    @DisplayName("Boost mode gives target creature +1/+1 until end of turn")
    void boostModeGivesPlusOnePlusOne() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Malus mode gives target creature -1/-1 until end of turn")
    void malusModeGivesMinusOneMinusOne() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(-1);
        assertThat(creature.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Both modes can target the same creature and pay escalate mana")
    void bothModesShareTargetAndEscalate() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Both modes without escalate mana are rejected")
    void bothModesWithoutEscalateManaRejected() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature cannot be targeted")
    void rejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AdarkarWastes());
        harness.setHand(player1, List.of(new BorrowedMalevolence()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(player1, 0, 1, 2,
                new int[]{0}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
