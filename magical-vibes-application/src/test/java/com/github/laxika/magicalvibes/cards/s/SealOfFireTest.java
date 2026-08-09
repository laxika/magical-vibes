package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SealOfFireTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Seal of Fire sacrifices it as a cost")
    void sacrificesAsCost() {
        addSealOfFire();

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Seal of Fire");
        harness.assertInGraveyard(player1, "Seal of Fire");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Seal of Fire deals 2 damage to a target player")
    void dealsDamageToPlayer() {
        harness.setLife(player2, 20);
        addSealOfFire();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Seal of Fire deals 2 damage to a target creature")
    void dealsDamageToCreature() {
        addSealOfFire();
        harness.addToBattlefield(player2, new GrizzlyBears());

        var targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Seal of Fire's ability fizzles when its target is removed")
    void fizzlesIfTargetRemoved() {
        addSealOfFire();
        harness.addToBattlefield(player2, new GrizzlyBears());

        var targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.gameLog.stream().map(entry -> entry.plainText()))
                .anyMatch(log -> log.contains("fizzles"));
    }

    private void addSealOfFire() {
        harness.addToBattlefield(player1, new SealOfFire());
    }
}
