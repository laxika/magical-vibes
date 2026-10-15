package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyscytheEngulfer.class, SuntailHawk.class, GrizzlyBears.class})
class SkyscytheEngulferTest extends BaseCardTest {

    @Test
    @DisplayName("Skyscythe Engulfer can't be blocked by a creature with flying")
    void cannotBeBlockedByFlyingCreature() {
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());

        Permanent attacker = addCreatureReady(player1, new SkyscytheEngulfer());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Skyscythe Engulfer can be blocked by a creature without flying")
    void canBeBlockedByNonFlyingCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent attacker = addCreatureReady(player1, new SkyscytheEngulfer());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void losingAllAbilitiesRemovesFlyingBlockerRestriction() {
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        Permanent attacker = addCreatureReady(player1, new SkyscytheEngulfer());
        attacker.setAttacking(true);
        attacker.setLosesAllAbilitiesUntilEndOfTurn(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reachAllowsBlockingFlyingAttacker() {
        Permanent attacker = addCreatureReady(player1, new SuntailHawk());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SkyscytheEngulfer());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SkyscytheEngulfer());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 4));

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
        harness.assertOnBattlefield(player1, "Skyscythe Engulfer");
    }
}
