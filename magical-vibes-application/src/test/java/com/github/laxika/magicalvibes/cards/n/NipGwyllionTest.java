package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NipGwyllion.class})
class NipGwyllionTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking a player gains controller life equal to combat damage dealt")
    void lifelinkGainsLifeOnAttack() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new NipGwyllion());

        declareAttackers(List.of(0));

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Attacker and blocker gain life even when both die in combat")
    void lifelinkGainsLifeFromLethalCreatureCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new NipGwyllion());
        addCreatureReady(player2, new NipGwyllion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        harness.assertInGraveyard(player1, "Nip Gwyllion");
        harness.assertInGraveyard(player2, "Nip Gwyllion");
        harness.assertNotOnBattlefield(player1, "Nip Gwyllion");
        harness.assertNotOnBattlefield(player2, "Nip Gwyllion");
    }

    @Test
    @DisplayName("Lifelink offsets simultaneous player damage before checking for a loss")
    void blockingLifelinkKeepsControllerAliveAtOneLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 1);
        addCreatureReady(player1, new NipGwyllion());
        addCreatureReady(player1, new NipGwyllion());
        addCreatureReady(player2, new NipGwyllion());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 1);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.assertInGraveyard(player2, "Nip Gwyllion");
    }
}
