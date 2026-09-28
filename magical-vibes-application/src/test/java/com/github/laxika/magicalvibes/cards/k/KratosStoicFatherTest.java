package com.github.laxika.magicalvibes.cards.k;

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

@CardUsed({KratosStoicFather.class, ZodiarkUmbralGod.class, GrizzlyBears.class})
class KratosStoicFatherTest extends BaseCardTest {

    @Test
    void getsExperienceWhenAControlledGodAttacks() {
        harness.addToBattlefield(player1, new KratosStoicFather());
        Permanent god = harness.addToBattlefieldAndReturn(player1, new ZodiarkUmbralGod());
        god.setSummoningSick(false);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void doesNotGetExperienceWhenOnlyNonGodsAttack() {
        harness.addToBattlefield(player1, new KratosStoicFather());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);

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
}
