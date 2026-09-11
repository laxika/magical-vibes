package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinPsychopath.class, GrizzlyBears.class})
class GoblinPsychopathTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, a lost flip redirects its next combat damage to its controller")
    void attackTriggerRedirectsLostFlip() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GoblinPsychopath());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat(player1);

        if (coinFlipWon()) {
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 15);
        } else {
            harness.assertLife(player1, 15);
            harness.assertLife(player2, 20);
        }
    }

    @Test
    @DisplayName("When it blocks, a lost flip redirects its next combat damage to its controller")
    void blockTriggerRedirectsLostFlip() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent blocker = addCreatureReady(player1, new GoblinPsychopath());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat(player2);

        if (coinFlipWon()) {
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
            harness.assertInGraveyard(player2, "Grizzly Bears");
        } else {
            harness.assertLife(player1, 15);
            harness.assertLife(player2, 20);
            assertThat(blocker.getMarkedDamage()).isEqualTo(2);
            harness.assertOnBattlefield(player2, "Grizzly Bears");
        }
    }

    private boolean coinFlipWon() {
        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText).toList();
        assertThat(logs).anyMatch(log -> log.contains("coin flip for Goblin Psychopath"));
        return logs.stream().anyMatch(log -> log.contains("wins the coin flip for Goblin Psychopath"));
    }
}
