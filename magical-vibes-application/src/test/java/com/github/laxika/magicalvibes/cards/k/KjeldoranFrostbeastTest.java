package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.Regeneration;
import com.github.laxika.magicalvibes.cards.s.ScaledWurm;
import com.github.laxika.magicalvibes.cards.s.ShieldBearer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KjeldoranFrostbeast.class, Regeneration.class, ScaledWurm.class, ShieldBearer.class})
class KjeldoranFrostbeastTest extends BaseCardTest {

    @Test
    @DisplayName("Every creature blocking Kjeldoran Frostbeast is destroyed at end of combat")
    void allBlockersDestroyedAtEndOfCombat() {
        Permanent frostbeast = addCreatureReady(player1, new KjeldoranFrostbeast());
        frostbeast.setAttacking(true);
        Permanent blocker1 = addCreatureReady(player2, new ShieldBearer());
        Permanent blocker2 = addCreatureReady(player2, new ShieldBearer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        resolveAllTriggers();

        harness.passBothPriorities();

        // Both blockers survive the Frostbeast's combat damage; the end-of-combat
        // destruction is what kills them.
        harness.handleCombatDamageAssigned(player2, 0, Map.of(blocker1.getId(), 1, blocker2.getId(), 1));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A creature blocked by Kjeldoran Frostbeast is destroyed at end of combat")
    void blockedAttackerDestroyedAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new ShieldBearer());
        attacker.setAttacking(true);
        addCreatureReady(player2, new KjeldoranFrostbeast());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getOriginalCard());
    }

    @Test
    @DisplayName("An unblocked Kjeldoran Frostbeast destroys nothing")
    void unblockedDestroysNothing() {
        Permanent frostbeast = addCreatureReady(player1, new KjeldoranFrostbeast());
        frostbeast.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ShieldBearer());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("A Kjeldoran Frostbeast that dies in combat does not destroy its former blocker")
    void sourceDyingInCombatDoesNotDestroyFormerBlocker() {
        Permanent frostbeast = addCreatureReady(player1, new KjeldoranFrostbeast());
        frostbeast.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ScaledWurm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(frostbeast.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("A regenerated Kjeldoran Frostbeast does not destroy a creature it no longer blocks")
    void regeneratedSourceDoesNotDestroyCreatureRemovedFromCombat() {
        Permanent frostbeast = addCreatureReady(player1, new KjeldoranFrostbeast());
        frostbeast.setAttacking(true);
        Permanent regeneration = harness.addToBattlefieldAndReturn(player1, new Regeneration());
        regeneration.setAttachedTo(frostbeast.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new ScaledWurm());
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(frostbeast.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }
}
