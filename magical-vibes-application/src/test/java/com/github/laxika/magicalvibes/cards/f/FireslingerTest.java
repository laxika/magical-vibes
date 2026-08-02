package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FireslingerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player and 1 damage to its controller")
    void damagesTargetPlayerAndController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyFireslinger(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, killing a 1/1, and 1 to its controller")
    void damagesTargetCreatureAndController() {
        harness.setLife(player1, 20);
        addReadyFireslinger(player1);
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    private Permanent addReadyFireslinger(Player player) {
        Permanent perm = new Permanent(new Fireslinger());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
