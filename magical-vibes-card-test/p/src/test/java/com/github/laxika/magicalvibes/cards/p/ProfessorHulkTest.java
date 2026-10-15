package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProfessorHulk.class, Forest.class})
class ProfessorHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to combat damage dealt to a player")
    void drawsEqualToCombatDamage() {
        Permanent hulk = addCreatureReady(player1, new ProfessorHulk());
        hulk.setAttacking(true);
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 6);
    }

    @Test
    @DisplayName("Combat damage to a creature does not draw cards")
    void noDrawWhenAllDamageIsDealtToBlocker() {
        addCreatureReady(player1, new ProfessorHulk());
        Permanent blocker = addCreatureReady(player2, new ProfessorHulk());
        harness.setLibrary(player1, List.of(new ProfessorHulk()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int defenderHandBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(defenderHandBefore);
    }

    @Test
    @DisplayName("Trample draws only for player damage even when Hulk dies in combat")
    void drawsForTrampleDamageAfterDyingInCombat() {
        addCreatureReady(player1, new ProfessorHulk());
        Permanent blocker = addCreatureReady(player2, new ProfessorHulk());
        blocker.setMarkedDamage(4);
        harness.setLibrary(player1, List.of(
                new ProfessorHulk(), new ProfessorHulk(), new ProfessorHulk(), new ProfessorHulk()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int defenderHandBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 4));
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Professor Hulk");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 4);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(defenderHandBefore);
    }
}
