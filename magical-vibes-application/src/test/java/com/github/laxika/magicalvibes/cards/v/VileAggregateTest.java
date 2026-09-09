package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MistIntruder;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VileAggregate.class, MistIntruder.class, GrizzlyBears.class})
class VileAggregateTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of colorless creatures its controller controls")
    void powerCountsColorlessCreaturesYouControl() {
        harness.addToBattlefield(player1, new VileAggregate());
        harness.addToBattlefield(player1, new MistIntruder());
        harness.addToBattlefield(player1, new GrizzlyBears());
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
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
    }
}
