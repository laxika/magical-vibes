package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KhenraEternal.class, GrizzlyBears.class})
class KhenraEternalTest extends BaseCardTest {

    @Test
    @DisplayName("Afflict 1: becoming blocked makes the defending player lose 1 life")
    void blockedAfflictsDefender() {
        Permanent atk = addCreatureReady(player1, new KhenraEternal());
        atk.setAttacking(true);

        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, new ArrayList<>());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Afflict is not a drain: the defender loses 1, the attacking player's life is unchanged.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Afflict does not trigger when the creature is not blocked")
    void unblockedDoesNotAfflict() {
        Permanent atk = addCreatureReady(player1, new KhenraEternal());
        atk.setAttacking(true);

        harness.setHand(player1, new ArrayList<>());
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        // Only the 2 combat damage lands; afflict adds nothing when the creature is unblocked
        // (17 would mean afflict fired erroneously).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Afflict triggers once even when two creatures block")
    void multipleBlockersAfflictOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new KhenraEternal());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KhenraEternal());
        addCreatureReady(player2, new KhenraEternal());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Afflict resolves even if its source leaves the battlefield")
    void afflictSurvivesSourceLeaving() {
        Permanent attacker = addCreatureReady(player1, new KhenraEternal());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KhenraEternal());
        harness.setHand(player1, List.of());
        harness.setLife(player2, 20);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.assertLife(player2, 20);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }
}
