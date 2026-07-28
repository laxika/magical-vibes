package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KjeldoranFrostbeastTest extends BaseCardTest {

    @Test
    @DisplayName("Every creature blocking Kjeldoran Frostbeast is destroyed at end of combat")
    void allBlockersDestroyedAtEndOfCombat() {
        Permanent frostbeast = addReadyFrostbeast(player1);
        frostbeast.setAttacking(true);
        Permanent spider1 = addReadySpider(player2);
        Permanent spider2 = addReadySpider(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        resolveAllTriggers();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).hasSize(2);

        harness.passBothPriorities();

        // Both 2/4 blockers survive the Frostbeast's combat damage; the end-of-combat
        // destruction is what kills them.
        harness.handleCombatDamageAssigned(player1, 0, Map.of(spider1.getId(), 1, spider2.getId(), 1));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A creature blocked by Kjeldoran Frostbeast is destroyed at end of combat")
    void blockedAttackerDestroyedAtEndOfCombat() {
        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);
        addReadyFrostbeast(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("An unblocked Kjeldoran Frostbeast destroys nothing")
    void unblockedDestroysNothing() {
        Permanent frostbeast = addReadyFrostbeast(player1);
        frostbeast.setAttacking(true);
        addReadySpider(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        harness.passBothPriorities();
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    private Permanent addReadyFrostbeast(Player player) {
        return addReady(player, new KjeldoranFrostbeast());
    }

    private Permanent addReadySpider(Player player) {
        return addReady(player, new GiantSpider());
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
