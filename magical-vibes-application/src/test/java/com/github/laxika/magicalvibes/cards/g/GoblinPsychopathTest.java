package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinPsychopath.class, GoblinBrigand.class})
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
        addCreatureReady(player2, new GoblinBrigand());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat(player2);

        if (coinFlipWon()) {
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
            harness.assertInGraveyard(player2, "Goblin Brigand");
        } else {
            harness.assertLife(player1, 15);
            harness.assertLife(player2, 20);
            assertThat(blocker.getMarkedDamage()).isEqualTo(2);
            harness.assertOnBattlefield(player2, "Goblin Brigand");
        }
    }

    @Test
    @DisplayName("When it attacks into a blocker, a lost flip redirects its combat damage to its controller")
    void attackTriggerRedirectsDamageFromAttackingCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GoblinPsychopath());
        addCreatureReady(player2, new GoblinBrigand());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat(player1);

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        if (coinFlipWon()) {
            harness.assertLife(player1, 20);
            harness.assertLife(player2, 20);
            harness.assertInGraveyard(player2, "Goblin Brigand");
        } else {
            harness.assertLife(player1, 15);
            harness.assertLife(player2, 20);
            harness.assertOnBattlefield(player1, "Goblin Psychopath");
            harness.assertOnBattlefield(player2, "Goblin Brigand");
        }
    }

    private boolean coinFlipWon() {
        assertThat(gameLogContains("coin flip for Goblin Psychopath")).isTrue();
        return gameLogContains("wins the coin flip for Goblin Psychopath");
    }
}
