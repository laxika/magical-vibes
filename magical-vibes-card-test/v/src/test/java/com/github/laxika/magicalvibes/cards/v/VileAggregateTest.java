package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HedronArchive;
import com.github.laxika.magicalvibes.cards.m.MistIntruder;
import com.github.laxika.magicalvibes.cards.s.SnappingGnarlid;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VileAggregate.class, MistIntruder.class, SnappingGnarlid.class, HedronArchive.class})
class VileAggregateTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of colorless creatures its controller controls")
    void powerCountsColorlessCreaturesYouControl() {
        harness.addToBattlefield(player1, new VileAggregate());
        harness.addToBattlefield(player1, new MistIntruder());
        harness.addToBattlefield(player1, new SnappingGnarlid());
        harness.addToBattlefield(player2, new MistIntruder());

        Permanent aggregate = findPermanent(player1, "Vile Aggregate");

        assertThat(gqs.getEffectivePower(gd, aggregate)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, aggregate)).isEqualTo(5);

        harness.addToBattlefield(player1, new MistIntruder());

        assertThat(gqs.getEffectivePower(gd, aggregate)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ingest exiles the top card of the damaged player's library")
    void ingestExilesTopCard() {
        Permanent aggregate = addCreatureReady(player1, new VileAggregate());
        aggregate.setAttacking(true);
        SnappingGnarlid topCard = new SnappingGnarlid();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
    }

    @Test
    void countsItselfButNotColorlessNoncreatures() {
        harness.addToBattlefield(player1, new VileAggregate());
        harness.addToBattlefield(player1, new HedronArchive());

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Vile Aggregate"))).isEqualTo(1);
    }

    @Test
    void powerDecreasesWhenColorlessCreatureLeaves() {
        harness.addToBattlefield(player1, new VileAggregate());
        harness.addToBattlefield(player1, new MistIntruder());
        Permanent aggregate = findPermanent(player1, "Vile Aggregate");
        assertThat(gqs.getEffectivePower(gd, aggregate)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Mist Intruder"));

        assertThat(gqs.getEffectivePower(gd, aggregate)).isEqualTo(1);
    }

    @Test
    void characteristicPowerAppliesInHandWithoutCountingItself() {
        VileAggregate aggregate = new VileAggregate();
        harness.setHand(player1, List.of(aggregate));
        assertThat(gqs.getEffectiveCardPower(gd, aggregate)).isZero();

        harness.addToBattlefield(player1, new MistIntruder());
        harness.addToBattlefield(player2, new MistIntruder());

        assertThat(gqs.getEffectiveCardPower(gd, aggregate)).isEqualTo(1);
    }

    @Test
    void dealingDamageOnlyToBlockerDoesNotTriggerIngest() {
        Permanent aggregate = addCreatureReady(player1, new VileAggregate());
        aggregate.setAttacking(true);
        addCreatureReady(player2, new SnappingGnarlid());
        SnappingGnarlid topCard = new SnappingGnarlid();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    void ingestWithEmptyLibraryDoesNotCausePlayerToLose() {
        Permanent aggregate = addCreatureReady(player1, new VileAggregate());
        aggregate.setAttacking(true);
        harness.setLibrary(player2, List.of());
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
    }

    @Test
    void trampleDamageTriggersIngestAndExilesOnlyOneCard() {
        Permanent aggregate = addCreatureReady(player1, new VileAggregate());
        aggregate.setAttacking(true);
        harness.addToBattlefield(player1, new MistIntruder());
        harness.addToBattlefield(player1, new MistIntruder());
        harness.addToBattlefield(player1, new MistIntruder());
        Permanent blocker = addCreatureReady(player2, new SnappingGnarlid());
        SnappingGnarlid topCard = new SnappingGnarlid();
        MistIntruder nextCard = new MistIntruder();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2, player2.getId(), 2));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Snapping Gnarlid");
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(nextCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
    }
}
