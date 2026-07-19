package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WanderingGoblinsTest extends BaseCardTest {

    @Test
    @DisplayName("No boost when controlling no basic land types")
    void noBoostWithoutBasicLandTypes() {
        Permanent goblins = harness.addToBattlefieldAndReturn(player1, new WanderingGoblins());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 0/3 base, +0/+0 with zero basic land types
        assertThat(goblins.getEffectivePower()).isZero();
        assertThat(goblins.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+0 for each basic land type among lands you control")
    void boostsPerBasicLandType() {
        Permanent goblins = harness.addToBattlefieldAndReturn(player1, new WanderingGoblins());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // 3 distinct basic land types => +3/+0 => 3/3
        assertThat(goblins.getEffectivePower()).isEqualTo(3);
        assertThat(goblins.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Duplicate basic land types count only once")
    void duplicateTypesCountOnce() {
        Permanent goblins = harness.addToBattlefieldAndReturn(player1, new WanderingGoblins());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Only one distinct type (Forest) => +1/+0
        assertThat(goblins.getEffectivePower()).isEqualTo(1);
        assertThat(goblins.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent goblins = harness.addToBattlefieldAndReturn(player1, new WanderingGoblins());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(goblins.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(goblins.getEffectivePower()).isZero();
        assertThat(goblins.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefieldAndReturn(player1, new WanderingGoblins());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
