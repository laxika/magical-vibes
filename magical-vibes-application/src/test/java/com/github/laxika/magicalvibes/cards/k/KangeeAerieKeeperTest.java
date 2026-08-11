package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KangeeAerieKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Kangee enters without feather counters when not kicked")
    void entersWithoutFeatherCountersWhenNotKicked() {
        harness.setHand(player1, List.of(new KangeeAerieKeeper()));
        addKangeeMana(0);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kangee = findPermanent(player1, "Kangee, Aerie Keeper");
        assertThat(kangee.getCounterCount(CounterType.FEATHER)).isZero();
    }

    @Test
    @DisplayName("Kicker X puts X feather counters on Kangee")
    void kickerPutsXFeatherCountersOnKangee() {
        harness.setHand(player1, List.of(new KangeeAerieKeeper()));
        addKangeeMana(3);
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 3, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true);
        harness.passBothPriorities();

        Permanent kangee = findPermanent(player1, "Kangee, Aerie Keeper");
        assertThat(kangee.getCounterCount(CounterType.FEATHER)).isEqualTo(3);
    }

    @Test
    @DisplayName("Other Birds get +1/+1 for each feather counter on Kangee")
    void boostsOtherBirdsByFeatherCounterCount() {
        Permanent kangee = addCreatureReady(player1, new KangeeAerieKeeper());
        kangee.setCounterCount(CounterType.FEATHER, 2);
        Permanent ownBird = addCreatureReady(player1, new BirdsOfParadise());
        Permanent opponentBird = addCreatureReady(player2, new BirdsOfParadise());
        Permanent nonBird = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBird)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBird)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonBird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonBird)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, kangee)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kangee)).isEqualTo(2);
    }

    private void addKangeeMana(int kickerX) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2 + kickerX);
    }
}
