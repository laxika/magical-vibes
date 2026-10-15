package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrambleweftBehemoth.class, GrizzlyBears.class})
class BrambleweftBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Brambleweft Behemoth deals excess combat damage to defending player via trample")
    void trampleDealsExcessDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent behemoth = addCreatureReady(player1, new BrambleweftBehemoth());
        behemoth.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Brambleweft Behemoth is 6/6, blocker is 2/2 → assign lethal (2) to blocker, excess (4) to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Trample permits assigning all combat damage to the blocker")
    void canAssignAllDamageToBlocker() {
        harness.setLife(player2, 20);
        Permanent behemoth = addCreatureReady(player1, new BrambleweftBehemoth());
        behemoth.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Brambleweft Behemoth");
    }
}
