package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.u.UrborgMindsucker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiverBoa.class, UrborgMindsucker.class})
class RiverBoaTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {G} grants a regeneration shield")
    void payGreenGrantsRegenerationShield() {
        Permanent boa = addCreatureReady(player1, new RiverBoa());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(boa.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves River Boa from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent boa = addCreatureReady(player1, new RiverBoa());
        boa.setRegenerationShield(1);
        boa.setBlocking(true);
        boa.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new UrborgMindsucker());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "River Boa");
        assertThat(boa.isTapped()).isTrue();
        assertThat(boa.getRegenerationShield()).isEqualTo(0);
        assertThat(boa.getMarkedDamage()).isZero();
        assertThat(boa.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("River Boa dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent boa = addCreatureReady(player1, new RiverBoa());
        boa.setBlocking(true);
        boa.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new UrborgMindsucker());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "River Boa");
        harness.assertInGraveyard(player1, "River Boa");
    }

    @Test
    @CardUsed(Island.class)
    @DisplayName("Islandwalk prevents blocking while the defender controls an Island")
    void islandwalkPreventsBlockingWhenDefenderControlsIsland() {
        harness.addToBattlefield(player2, new Island());
        Permanent blocker = addCreatureReady(player2, new UrborgMindsucker());
        Permanent attacker = addCreatureReady(player1, new RiverBoa());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Islandwalk does not prevent blocking when the defender controls no Island")
    void islandwalkAllowsBlockingWithoutIsland() {
        Permanent blocker = addCreatureReady(player2, new UrborgMindsucker());
        Permanent attacker = addCreatureReady(player1, new RiverBoa());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
