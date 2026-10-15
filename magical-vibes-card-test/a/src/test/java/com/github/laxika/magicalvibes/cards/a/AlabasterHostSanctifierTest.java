package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AlabasterHostSanctifier.class)
class AlabasterHostSanctifierTest extends BaseCardTest {

    @Test
    @DisplayName("Lifelink gains life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent sanctifier = addCreatureReady(player1, new AlabasterHostSanctifier());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(sanctifier)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Both controllers gain life when attacking and blocking Sanctifiers die in combat")
    void lifelinkGainsLifeEvenWhenSourceDiesInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new AlabasterHostSanctifier());
        addCreatureReady(player2, new AlabasterHostSanctifier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        harness.assertNotOnBattlefield(player1, "Alabaster Host Sanctifier");
        harness.assertNotOnBattlefield(player2, "Alabaster Host Sanctifier");
        harness.assertInGraveyard(player1, "Alabaster Host Sanctifier");
        harness.assertInGraveyard(player2, "Alabaster Host Sanctifier");
    }
}
