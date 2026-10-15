package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MoonriseCleric;
import com.github.laxika.magicalvibes.cards.h.HarvestriteHost;
import com.github.laxika.magicalvibes.cards.f.FlamecacheGecko;
import com.github.laxika.magicalvibes.cards.p.PersistentMarshstalker;
import com.github.laxika.magicalvibes.cards.b.BakersbaneDuo;
import com.github.laxika.magicalvibes.cards.i.IntoTheFloodMaw;
import com.github.laxika.magicalvibes.cards.t.ThreeTreeMascot;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValleyRotcaller.class, BakersbaneDuo.class, MoonriseCleric.class, FlamecacheGecko.class,
        PersistentMarshstalker.class, HarvestriteHost.class, ThreeTreeMascot.class, IntoTheFloodMaw.class})
class ValleyRotcallerTest extends BaseCardTest {

    @Test
    void attacksAndDrainsForOtherMatchingCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ValleyRotcaller());
        addCreatureReady(player1, new BakersbaneDuo());
        addCreatureReady(player1, new MoonriseCleric());
        addCreatureReady(player1, new FlamecacheGecko());
        addCreatureReady(player1, new PersistentMarshstalker());
        addCreatureReady(player1, new HarvestriteHost());
        addCreatureReady(player2, new BakersbaneDuo());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
    }

    @Test
    void sourceAndOpponentsAreNotCounted() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ValleyRotcaller());
        addCreatureReady(player1, new HarvestriteHost());
        addCreatureReady(player2, new BakersbaneDuo());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void countIsEvaluatedAsTheTriggerResolves() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ValleyRotcaller());
        addCreatureReady(player1, new BakersbaneDuo());

        declareAttackers(player1, List.of(0));
        harness.addToBattlefield(player1, new FlamecacheGecko());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void changelingCountsOnlyOnceDespiteHavingAllFourMatchingTypes() {
        addCreatureReady(player1, new ValleyRotcaller());
        addCreatureReady(player1, new ThreeTreeMascot());
        addCreatureReady(player2, new HarvestriteHost());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    void twoRotcallersCountEachOtherAndTriggerIndependently() {
        addCreatureReady(player1, new ValleyRotcaller());
        addCreatureReady(player1, new ValleyRotcaller());
        addCreatureReady(player1, new BakersbaneDuo());
        addCreatureReady(player2, new HarvestriteHost());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
    }

    @Test
    void triggerStillResolvesAfterSourceIsReturnedToHand() {
        Permanent rotcaller = addCreatureReady(player1, new ValleyRotcaller());
        addCreatureReady(player1, new BakersbaneDuo());
        addCreatureReady(player2, new HarvestriteHost());
        harness.setHand(player2, List.of(new IntoTheFloodMaw()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.castInstantWithGift(player2, 0, rotcaller.getId(), false);
        resolveAllTriggers();

        harness.assertInHand(player1, "Valley Rotcaller");
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    void matchingCreatureReturnedInResponseIsNotCounted() {
        addCreatureReady(player1, new ValleyRotcaller());
        Permanent squirrel = addCreatureReady(player1, new BakersbaneDuo());
        addCreatureReady(player2, new HarvestriteHost());
        harness.setHand(player2, List.of(new IntoTheFloodMaw()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.castInstantWithGift(player2, 0, squirrel.getId(), false);
        resolveAllTriggers();

        harness.assertInHand(player1, "Bakersbane Duo");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void menaceRejectsOneBlocker() {
        addCreatureReady(player1, new ValleyRotcaller());
        addCreatureReady(player2, new HarvestriteHost());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void menaceAllowsTwoBlockers() {
        addCreatureReady(player1, new ValleyRotcaller());
        Permanent first = addCreatureReady(player2, new HarvestriteHost());
        Permanent second = addCreatureReady(player2, new HarvestriteHost());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
