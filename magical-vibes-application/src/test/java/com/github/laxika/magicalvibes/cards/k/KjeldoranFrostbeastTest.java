package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.Regeneration;
import com.github.laxika.magicalvibes.cards.s.ScaledWurm;
import com.github.laxika.magicalvibes.cards.s.ShieldBearer;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KjeldoranFrostbeast.class, Regeneration.class, ScaledWurm.class, ShieldBearer.class,
        SwordsToPlowshares.class})
class KjeldoranFrostbeastTest extends BaseCardTest {

    @Test
    @DisplayName("Every creature blocking Kjeldoran Frostbeast is destroyed at end of combat")
    void allBlockersDestroyedAtEndOfCombat() {
        addCreatureReady(player1, new KjeldoranFrostbeast());
        Permanent blocker1 = addCreatureReady(player2, new ShieldBearer());
        Permanent blocker2 = addCreatureReady(player2, new ShieldBearer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        resolveAllTriggers();

        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        // Both blockers survive the Frostbeast's combat damage; the end-of-combat
        // destruction is what kills them.
        harness.handleCombatDamageAssigned(player2, 0, Map.of(blocker1.getId(), 1, blocker2.getId(), 1));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A creature blocked by Kjeldoran Frostbeast is destroyed at end of combat")
    void blockedAttackerDestroyedAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new ShieldBearer());
        addCreatureReady(player2, new KjeldoranFrostbeast());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getOriginalCard());
    }

    @Test
    @DisplayName("An unblocked Kjeldoran Frostbeast destroys nothing")
    void unblockedDestroysNothing() {
        addCreatureReady(player1, new KjeldoranFrostbeast());
        Permanent blocker = addCreatureReady(player2, new ShieldBearer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("A Kjeldoran Frostbeast that dies in combat does not destroy its former blocker")
    void sourceDyingInCombatDoesNotDestroyFormerBlocker() {
        Permanent frostbeast = addCreatureReady(player1, new KjeldoranFrostbeast());
        Permanent blocker = addCreatureReady(player2, new ScaledWurm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(frostbeast.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("A regenerated Kjeldoran Frostbeast does not destroy a creature it no longer blocks")
    void regeneratedSourceDoesNotDestroyCreatureRemovedFromCombat() {
        Permanent frostbeast = addCreatureReady(player1, new KjeldoranFrostbeast());
        Permanent regeneration = harness.addToBattlefieldAndReturn(player1, new Regeneration());
        regeneration.setAttachedTo(frostbeast.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        Permanent blocker = addCreatureReady(player2, new ScaledWurm());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveAllTriggers();
        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(frostbeast.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("A blocker can regenerate from Frostbeast's end-of-combat destruction")
    void blockerCanRegenerateFromEndOfCombatDestruction() {
        addCreatureReady(player1, new KjeldoranFrostbeast());
        Permanent blocker = addCreatureReady(player2, new ShieldBearer());
        Permanent regeneration = harness.addToBattlefieldAndReturn(player2, new Regeneration());
        regeneration.setAttachedTo(blocker.getId());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Removing Frostbeast after its ability triggers does not save its blocker")
    void sourceRemovedAfterTriggerStillDestroysBlocker() {
        Permanent frostbeast = addCreatureReady(player1, new KjeldoranFrostbeast());
        Permanent blocker = addCreatureReady(player2, new ShieldBearer());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, frostbeast.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(frostbeast);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getOriginalCard());
    }
}
