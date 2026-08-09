package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FanaticalDevotionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature regenerates the target creature")
    void sacrificesCreatureAndRegeneratesTarget() {
        Permanent devotion = harness.addToBattlefieldAndReturn(player1, new FanaticalDevotion());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID fodderId = harness.getPermanentId(player1, "Grizzly Bears");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(devotion.getId()).isNotEqualTo(fodderId);

        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new FanaticalDevotion());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void requiresCreatureSacrifice() {
        harness.addToBattlefield(player1, new FanaticalDevotion());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
