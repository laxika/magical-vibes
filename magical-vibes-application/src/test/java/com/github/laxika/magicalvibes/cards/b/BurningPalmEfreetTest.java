package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurningPalmEfreetTest extends BaseCardTest {

    private void payAbility() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Deals 2 damage to a target flyer and strips its flying")
    void damagesAndStripsFlying() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        harness.addToBattlefield(player2, new AirElemental());
        payAbility();

        Permanent target = findPermanent(player2, "Air Elemental");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Kills a 1/1 flyer")
    void killsSmallFlyer() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        harness.addToBattlefield(player2, new SuntailHawk());
        payAbility();

        Permanent target = findPermanent(player2, "Suntail Hawk");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Flying comes back at end of turn")
    void flyingReturnsAtEndOfTurn() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        harness.addToBattlefield(player2, new AirElemental());
        payAbility();

        Permanent target = findPermanent(player2, "Air Elemental");
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player1, new BurningPalmEfreet());
        harness.addToBattlefield(player2, new LlanowarElves());
        payAbility();

        Permanent target = findPermanent(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }
}
