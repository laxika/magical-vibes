package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GhituFireEaterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage (its power) to target player")
    void dealsPowerDamageToPlayer() {
        addReadyFireEater(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals damage equal to boosted power to target player")
    void dealsBoostedDamageToPlayer() {
        Permanent fireEater = addReadyFireEater(player1);
        fireEater.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // power becomes 2+2 = 4
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Deals 2 damage to target creature, killing a 2/2")
    void dealsPowerDamageToCreature() {
        addReadyFireEater(player1);
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrifices itself as a cost when the ability is activated")
    void sacrificesSelfAsCost() {
        addReadyFireEater(player1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ghitu Fire-Eater"));
        harness.assertInGraveyard(player1, "Ghitu Fire-Eater");
    }

    @Test
    @DisplayName("Still deals its last-known power after being sacrificed")
    void dealsLastKnownPowerAfterSacrifice() {
        addReadyFireEater(player1);
        harness.setLife(player2, 20);

        // Sacrifice is paid on activation, so the source is already gone when the
        // ability resolves; it uses the last-known power snapshot (2).
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyFireEater(Player player) {
        GhituFireEater card = new GhituFireEater();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
