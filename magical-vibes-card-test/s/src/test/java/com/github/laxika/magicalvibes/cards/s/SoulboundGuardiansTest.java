package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.m.MakindiGriffin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulboundGuardians.class, GlorySeeker.class, MakindiGriffin.class})
class SoulboundGuardiansTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack because it has defender")
    void cannotAttack() {
        Permanent guardians = addCreatureReady(player1, new SoulboundGuardians());
        harness.addToBattlefield(player2, new GlorySeeker());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(guardians))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can block a creature with flying")
    void canBlockCreatureWithFlying() {
        Permanent attacker = addCreatureReady(player1, new MakindiGriffin());
        Permanent guardians = addCreatureReady(player2, new SoulboundGuardians());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(guardians),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(guardians.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can block a ground creature even while summoning sick")
    void canBlockGroundCreatureWhileSummoningSick() {
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        Permanent guardians = harness.addToBattlefieldAndReturn(player2, new SoulboundGuardians());
        guardians.setSummoningSick(true);

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(guardians),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(guardians.isBlocking()).isTrue();
    }
}
