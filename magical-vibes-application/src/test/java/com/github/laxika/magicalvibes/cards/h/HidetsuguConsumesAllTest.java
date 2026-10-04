package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MastersRebuke;
import com.github.laxika.magicalvibes.cards.n.NetworkDisruptor;
import com.github.laxika.magicalvibes.cards.v.VesselOfTheAllConsuming;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HidetsuguConsumesAll.class, VesselOfTheAllConsuming.class, Memnite.class,
        Mountain.class, GrizzlyBears.class, NetworkDisruptor.class, MastersRebuke.class})
class HidetsuguConsumesAllTest extends BaseCardTest {

    @Test
    void chapterIDestroysNonlandPermanentsWithManaValueOneOrLess() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new GrizzlyBears());
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() instanceof Memnite);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard() instanceof Mountain);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getCard() instanceof GrizzlyBears);
    }

    @Test
    void chapterIIExilesAllGraveyards() {
        harness.setGraveyard(player1, List.of(new Memnite()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof Memnite);
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    void chapterIIITransformsIntoVesselUnderControllerControl() {
        addSagaWithLore(2);

        advanceToNextChapter();

        Permanent vessel = findPermanent(player1, "Vessel of the All-Consuming");
        assertThat(vessel.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof HidetsuguConsumesAll);
    }

    @Test
    void vesselPutsPlusOneCounterOnItWhenItDealsDamage() {
        Permanent vessel = addVessel();
        vessel.setPowerModifier(2);
        vessel.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(vessel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void vesselMakesDamagedPlayerLoseAfterTenDamageThisTurn() {
        Permanent vessel = addVessel();
        vessel.setPowerModifier(7);
        vessel.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void chapterIDestroysManaValueOnePermanentsOnBothBattlefields() {
        harness.addToBattlefield(player1, new NetworkDisruptor());
        harness.addToBattlefield(player2, new NetworkDisruptor());
        addSagaWithLore(0);

        advanceToNextChapter();

        harness.assertNotOnBattlefield(player1, "Network Disruptor");
        harness.assertNotOnBattlefield(player2, "Network Disruptor");
        harness.assertInGraveyard(player1, "Network Disruptor");
        harness.assertInGraveyard(player2, "Network Disruptor");
        harness.assertOnBattlefield(player1, "Hidetsugu Consumes All");
    }

    @Test
    void castingTheSagaTriggersChapterIImmediately() {
        harness.addToBattlefield(player2, new NetworkDisruptor());

        harness.castFromHand(player1, new HidetsuguConsumesAll(), "{1}{B}{R}");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Network Disruptor");
        harness.assertOnBattlefield(player1, "Hidetsugu Consumes All");
    }

    @Test
    void noncombatDamageToACreatureAddsACounter() {
        Permanent vessel = addVessel();
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new NetworkDisruptor());
        harness.setHand(player1, List.of(new MastersRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, List.of(vessel.getId(), victim.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Network Disruptor");
        assertThat(vessel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void chapterIIIProducesANewPermanentWithoutTheSagasCounters() {
        Permanent saga = addSagaWithLore(2);
        saga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToNextChapter();

        Permanent vessel = findPermanent(player1, "Vessel of the All-Consuming");
        assertThat(vessel.getId()).isNotEqualTo(saga.getId());
        assertThat(vessel.getCounterCount(CounterType.LORE)).isZero();
        assertThat(vessel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(vessel.isSummoningSick()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Hidetsugu Consumes All");
    }

    @Test
    void chapterISparesAnAlreadyTransformedVessel() {
        addSagaWithLore(2);
        advanceToNextChapter();
        Permanent vessel = findPermanent(player1, "Vessel of the All-Consuming");
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vessel);
    }

    @Test
    void nineDamageDoesNotMakeThePlayerLose() {
        Permanent vessel = addVessel();
        vessel.setPowerModifier(6);
        vessel.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 11);
        assertThat(vessel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void damageFromDifferentVesselsDoesNotCombineForTheLossCondition() {
        Permanent first = addVessel();
        Permanent second = addVessel();
        first.setPowerModifier(2);
        second.setPowerModifier(2);
        first.setAttacking(true);
        second.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 10);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void trampleDamageToBothCreatureAndPlayerAddsOnlyOneCounter() {
        Permanent vessel = addVessel();
        vessel.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new NetworkDisruptor());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 2));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Network Disruptor");
        assertThat(vessel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void zeroDamageDoesNotAddACounter() {
        Permanent vessel = addVessel();
        vessel.setPowerModifier(-3);
        vessel.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(vessel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new HidetsuguConsumesAll());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addVessel() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new VesselOfTheAllConsuming());
        vessel.setSummoningSick(false);
        return vessel;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
