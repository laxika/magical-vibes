package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MineBearerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and destroys the attacking creature")
    void sacrificesItselfAndDestroysAttacker() {
        addReadyMineBearer(player1);
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mine Bearer");
        harness.assertInGraveyard(player1, "Mine Bearer");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttacker() {
        addReadyMineBearer(player1);
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMineBearer(Player player) {
        Permanent mineBearer = new Permanent(new MineBearer());
        mineBearer.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(mineBearer);
        return mineBearer;
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }
}
