package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TwoHeadedCerberusTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked double strike deals damage in both combat phases")
    void unblockedDoubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent attacker = addReadyCerberus(player1);
        attacker.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Double strike kills a small blocker before it can deal regular damage")
    void doubleStrikeKillsSmallBlockerBeforeRegularDamage() {
        Permanent attacker = addReadyCerberus(player1);
        attacker.setAttacking(true);

        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        Permanent blocker = new Permanent(smallCreature);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blocker);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Two-Headed Cerberus");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addReadyCerberus(Player player) {
        Permanent permanent = new Permanent(new TwoHeadedCerberus());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
