package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BenthicBiomancerTest extends BaseCardTest {

    @Test
    @DisplayName("Adapt puts a +1/+1 counter on Benthic Biomancer and loots")
    void adaptAddsCounterAndLoots() {
        Permanent biomancer = addBiomancer();
        Shock discard = new Shock();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discard));
        harness.setLibrary(player1, List.of(drawn));
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(biomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        int discardIndex = gd.playerHands.get(player1.getId()).indexOf(discard);
        harness.handleCardChosen(player1, discardIndex);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
    }

    @Test
    @DisplayName("Adapt can be activated once Benthic Biomancer has a +1/+1 counter")
    void adaptCanBeActivatedWithCounter() {
        Permanent biomancer = addBiomancer();
        biomancer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(biomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adapt does not put a second counter when the ability resolves after a counter is added")
    void adaptChecksForCountersOnResolution() {
        Permanent biomancer = addBiomancer();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        biomancer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(biomancer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addBiomancer() {
        Permanent biomancer = addCreatureReady(player1, new BenthicBiomancer());
        biomancer.setSummoningSick(false);
        return biomancer;
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
