package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NobleOx.class, AirElemental.class, GrizzlyBears.class})
class NobleOxTest extends BaseCardTest {

    @Test
    @DisplayName("ETB blocks every unblocked creature attacking its controller")
    void entersAndBlocksAllUnblockedAttackers() {
        Permanent groundAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent flyingAttacker = addCreatureReady(player1, new AirElemental());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));

        castNobleOxInCombat();
        Permanent nobleOx = findPermanent(player2, "Noble Ox");

        assertThat(nobleOx.getBlockingTargetIds())
                .containsExactlyInAnyOrder(groundAttacker.getId(), flyingAttacker.getId());
        assertThat(gqs.isBlockedByAnyCreature(gd, groundAttacker)).isTrue();
        assertThat(gqs.isBlockedByAnyCreature(gd, flyingAttacker)).isTrue();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Noble Ox can block any number of creatures")
    void canBlockAnyNumberOfCreatures() {
        Permanent nobleOx = addCreatureReady(player2, new NobleOx());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));

        assertThat(nobleOx.getBlockingTargetIds())
                .containsExactlyInAnyOrder(firstAttacker.getId(), secondAttacker.getId());
    }

    @Test
    @DisplayName("Attackers are not unblocked before blockers have been declared")
    void enteringBeforeBlockersAreDeclaredDoesNotBlockAttackers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        castNobleOxInCombat();

        Permanent nobleOx = findPermanent(player2, "Noble Ox");
        assertThat(nobleOx.getBlockingTargetIds()).isEmpty();
        assertThat(gqs.isBlockedByAnyCreature(gd, attacker)).isFalse();
    }

    @Test
    @DisplayName("ETB leaves already blocked attackers with their existing blocker")
    void entersAndBlocksOnlyUnblockedAttackers() {
        Permanent blockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent unblockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent existingBlocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        castNobleOxInCombat();

        Permanent nobleOx = findPermanent(player2, "Noble Ox");
        assertThat(nobleOx.getBlockingTargetIds()).containsExactly(unblockedAttacker.getId());
        assertThat(existingBlocker.getBlockingTargetIds()).containsExactly(blockedAttacker.getId());
    }

    private void castNobleOxInCombat() {
        harness.setHand(player2, List.of(new NobleOx()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.withAutoStop(gd.currentStep, () -> {
            harness.castCreature(player2, 0);
            resolveAllTriggers();
        });
    }
}
