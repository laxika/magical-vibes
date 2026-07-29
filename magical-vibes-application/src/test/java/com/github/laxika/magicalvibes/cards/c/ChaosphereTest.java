package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChaosphereTest extends BaseCardTest {

    @Test
    @DisplayName("A flying creature can't block a creature without flying")
    void flierCannotBlockGroundCreature() {
        addChaosphere();
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures with flying can block only creatures with flying");
    }

    @Test
    @DisplayName("A flying creature can still block a flying attacker")
    void flierCanBlockFlier() {
        addChaosphere();
        addCreatureReady(player1, new AirElemental()).setAttacking(true);
        addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("A creature without flying gains reach and can block a flying attacker")
    void groundCreatureGainsReach() {
        addChaosphere();
        addCreatureReady(player1, new AirElemental()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Without Chaosphere a creature without flying still can't block a flier")
    void noReachWithoutChaosphere() {
        addCreatureReady(player1, new AirElemental()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addChaosphere() {
        Permanent perm = new Permanent(new Chaosphere());
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }
}
