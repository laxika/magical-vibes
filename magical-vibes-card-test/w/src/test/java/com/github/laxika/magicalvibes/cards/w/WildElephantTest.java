package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildElephant.class, FeralShadow.class})
class WildElephantTest extends BaseCardTest {

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new WildElephant());
        Permanent blocker = addCreatureReady(player2, new FeralShadow());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Trample permits assigning all combat damage to the blocker")
    void canAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new WildElephant());
        Permanent blocker = addCreatureReady(player2, new FeralShadow());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));

        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Feral Shadow");
    }

    @Test
    @DisplayName("Trample assigns lethal damage to each blocker before excess reaches the player")
    void tramplesOverMultipleBlockers() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WildElephant());
        Permanent firstBlocker = addCreatureReady(player2, new FeralShadow());
        Permanent secondBlocker = addCreatureReady(player2, new FeralShadow());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 1,
                secondBlocker.getId(), 1,
                player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Wild Elephant");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(firstBlocker, secondBlocker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Feral Shadow", "Feral Shadow");
    }
}
