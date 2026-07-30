package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinFireslingerTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent fireslinger = addReadyFireslinger(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(fireslinger.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability can't target a creature")
    void cannotTargetCreature() {
        addReadyFireslinger(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyFireslinger(Player player) {
        Permanent perm = new Permanent(new GoblinFireslinger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
