package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RonomUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability destroys target enchantment")
    void sacrificeAbilityDestroysTargetEnchantment() {
        harness.addToBattlefield(player1, new RonomUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ronom Unicorn");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Can target an enchantment controlled by its controller")
    void canTargetOwnEnchantment() {
        harness.addToBattlefield(player1, new RonomUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ronom Unicorn");
        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantmentPermanent() {
        harness.addToBattlefield(player1, new RonomUnicorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
