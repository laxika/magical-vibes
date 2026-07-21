package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnbenderTineTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps Unbender Tine")
    void activatingTapsUnbenderTine() {
        harness.addToBattlefield(player1, new UnbenderTine());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);

        Permanent unbenderTine = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(unbenderTine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps any tapped permanent, not just artifacts")
    void untapsTappedCreature() {
        harness.addToBattlefield(player1, new UnbenderTine());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);
        target.tap();
        assertThat(target.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap own tapped permanent")
    void canUntapOwnPermanent() {
        harness.addToBattlefield(player1, new UnbenderTine());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        Permanent target = gd.playerBattlefields.get(player1.getId()).get(1);
        target.tap();

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself — must be another permanent")
    void cannotTargetItself() {
        harness.addToBattlefield(player1, new UnbenderTine());
        UUID unbenderTineId = harness.getPermanentId(player1, "Unbender Tine");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, unbenderTineId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another permanent");
    }
}
