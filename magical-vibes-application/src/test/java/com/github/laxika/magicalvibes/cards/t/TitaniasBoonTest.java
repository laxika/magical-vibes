package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TitaniasBoon.class, CoralMerfolk.class, Forest.class})
class TitaniasBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature you control")
    void putsCounterOnEachControlledCreature() {
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.addToBattlefield(player1, new CoralMerfolk());

        harness.castFromHand(player1, new TitaniasBoon(), "{3}{G}");
        harness.passBothPriorities();

        List<Permanent> merfolk = findPermanents(player1, "Coral Merfolk");

        assertThat(merfolk).hasSize(2);
        assertThat(merfolk).allSatisfy(merfolkPermanent ->
                assertThat(merfolkPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("Does not affect opponent's creatures or noncreature permanents")
    void affectsOnlyControlledCreatures() {
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new CoralMerfolk());

        harness.castFromHand(player1, new TitaniasBoon(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Coral Merfolk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player2, "Coral Merfolk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(findPermanent(player1, "Forest")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Adds to existing +1/+1 counters")
    void addsToExistingCounters() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        merfolk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castFromHand(player1, new TitaniasBoon(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(merfolk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
