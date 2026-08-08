package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SplitTailMikoTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 2 damage dealt to a target creature")
    void preventsNextDamageToCreature() {
        addCreatureReady(player1, new SplitTailMiko());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        target.setBlocking(true);
        target.addBlockingTarget(0);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Prevents the next 2 damage dealt to a target player")
    void preventsNextDamageToPlayer() {
        addCreatureReady(player1, new SplitTailMiko());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        resolveCombat(player1);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new SplitTailMiko());
        Permanent forest = addCreatureReady(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = forest.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
