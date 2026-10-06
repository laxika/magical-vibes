package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.c.CoverOfWinter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RonomUnicorn.class, CoverOfWinter.class, BorealDruid.class})
class RonomUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped summoning-sick Unicorn pays sacrifice cost before resolution")
    void tappedSummoningSickUnicornPaysSacrificeCostBeforeResolution() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new RonomUnicorn());
        unicorn.setTapped(true);
        unicorn.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoverOfWinter());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Ronom Unicorn");
        harness.assertInGraveyard(player1, "Ronom Unicorn");
        harness.assertOnBattlefield(player2, "Cover of Winter");
        harness.assertNotInGraveyard(player2, "Cover of Winter");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cover of Winter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice ability destroys target enchantment")
    void sacrificeAbilityDestroysTargetEnchantment() {
        harness.addToBattlefield(player1, new RonomUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoverOfWinter());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ronom Unicorn");
        harness.assertInGraveyard(player2, "Cover of Winter");
    }

    @Test
    @DisplayName("Can target an enchantment controlled by its controller")
    void canTargetOwnEnchantment() {
        harness.addToBattlefield(player1, new RonomUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CoverOfWinter());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ronom Unicorn");
        harness.assertInGraveyard(player1, "Cover of Winter");
    }

    @Test
    @DisplayName("Cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantmentPermanent() {
        harness.addToBattlefield(player1, new RonomUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice cost is paid even if the target leaves before resolution")
    void sacrificesItselfWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new RonomUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoverOfWinter());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent -> permanent.getId().equals(target.getId()));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player1, "Ronom Unicorn");
        harness.assertNotOnBattlefield(player2, "Cover of Winter");
    }
}
