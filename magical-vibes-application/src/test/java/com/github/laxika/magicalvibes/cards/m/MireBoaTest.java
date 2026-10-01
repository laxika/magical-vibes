package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PouncingWurm;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
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

@CardUsed({MireBoa.class, PouncingWurm.class, UrborgTombOfYawgmoth.class})
class MireBoaTest extends BaseCardTest {

    @Test
    @DisplayName("Mire Boa cannot be blocked when defending player controls a Swamp")
    void cannotBeBlockedWhenDefenderControlsSwamp() {
        harness.addToBattlefield(player2, new UrborgTombOfYawgmoth());
        Permanent blocker = addCreatureReady(player2, new PouncingWurm());
        Permanent boa = addAttackingBoa();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(boa);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Mire Boa can be blocked when defending player does not control a Swamp")
    void canBeBlockedWhenDefenderDoesNotControlSwamp() {
        Permanent blocker = addCreatureReady(player2, new PouncingWurm());
        Permanent boa = addAttackingBoa();

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(boa);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("{G} grants Mire Boa a regeneration shield")
    void regenerationShield() {
        Permanent boa = addCreatureReady(player1, new MireBoa());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(boa.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Mire Boa from lethal combat damage")
    void regenerationShieldSavesMireBoaFromLethalCombatDamage() {
        Permanent boa = addAttackingBoa();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new PouncingWurm());
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(boa);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(boa);
        assertThat(boa.isTapped()).isTrue();
        assertThat(boa.isAttacking()).isFalse();
        assertThat(boa.getRegenerationShield()).isZero();
        assertThat(boa.getMarkedDamage()).isZero();
    }

    private Permanent addAttackingBoa() {
        Permanent boa = addCreatureReady(player1, new MireBoa());
        boa.setAttacking(true);
        return boa;
    }
}
