package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.m.MtendaGriffin;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Chaosphere.class, MtendaGriffin.class, FemerefScouts.class})
class ChaosphereTest extends BaseCardTest {

    @Test
    @DisplayName("A flying creature can't block a creature without flying")
    void flierCannotBlockGroundCreature() {
        addChaosphere();
        addCreatureReady(player1, new FemerefScouts()).setAttacking(true);
        addCreatureReady(player2, new MtendaGriffin());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures with flying can block only creatures with flying");
    }

    @Test
    @DisplayName("A flying creature can still block a flying attacker")
    void flierCanBlockFlier() {
        addChaosphere();
        addCreatureReady(player1, new MtendaGriffin()).setAttacking(true);
        addCreatureReady(player2, new MtendaGriffin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("A creature without flying gains reach and can block a flying attacker")
    void groundCreatureGainsReach() {
        addChaosphere();
        addCreatureReady(player1, new MtendaGriffin()).setAttacking(true);
        addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Without Chaosphere a creature without flying still can't block a flier")
    void noReachWithoutChaosphere() {
        addCreatureReady(player1, new MtendaGriffin()).setAttacking(true);
        addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addChaosphere() {
        harness.addToBattlefieldAndReturn(player1, new Chaosphere());
    }
}
