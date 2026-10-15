package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZodiarkUmbralGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KratosStoicFather.class, ZodiarkUmbralGod.class, GrizzlyBears.class, AmoeboidChangeling.class})
class KratosStoicFatherTest extends BaseCardTest {

    @Test
    void getsExperienceWhenAControlledGodAttacks() {
        harness.addToBattlefield(player1, new KratosStoicFather());
        addCreatureReady(player1, new ZodiarkUmbralGod());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void doesNotGetExperienceWhenOnlyNonGodsAttack() {
        harness.addToBattlefield(player1, new KratosStoicFather());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void getsExperienceWhenAnyGodDies() {
        harness.addToBattlefield(player1, new KratosStoicFather());
        Permanent god = harness.addToBattlefieldAndReturn(player2, new ZodiarkUmbralGod());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, god));
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void getsExperienceWhenKratosDies() {
        Permanent kratos = harness.addToBattlefieldAndReturn(player1, new KratosStoicFather());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, kratos));
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void putsCountersOnTargetCreatureEqualToExperienceAtEndStep() {
        harness.addToBattlefield(player1, new KratosStoicFather());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerExperienceCounters.put(player1.getId(), 3);

        advanceToEndStep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    void multipleAttackingGodsGiveOnlyOneExperienceCounter() {
        addCreatureReady(player1, new KratosStoicFather());
        addCreatureReady(player1, new ZodiarkUmbralGod());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void opposingGodAttackingDoesNotGiveExperience() {
        harness.addToBattlefield(player1, new KratosStoicFather());
        addCreatureReady(player2, new ZodiarkUmbralGod());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    void nonGodDyingDoesNotGiveExperience() {
        harness.addToBattlefield(player1, new KratosStoicFather());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    void ownDeathDoesNotGiveExperienceAfterLosingGodType() {
        Permanent kratos = harness.addToBattlefieldAndReturn(player1, new KratosStoicFather());
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.activateAbility(player1, 1, 1, null, kratos.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, kratos));
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void endStepCountsExperienceAtResolution() {
        harness.addToBattlefield(player1, new KratosStoicFather());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent god = harness.addToBattlefieldAndReturn(player2, new ZodiarkUmbralGod());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, god));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void opponentEndStepDoesNotPutCountersOnCreatures() {
        Permanent kratos = harness.addToBattlefieldAndReturn(player1, new KratosStoicFather());
        gd.playerExperienceCounters.put(player1.getId(), 3);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(kratos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
