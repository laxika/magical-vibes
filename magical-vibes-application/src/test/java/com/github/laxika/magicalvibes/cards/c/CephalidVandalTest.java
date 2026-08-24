package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CephalidVandal.class, Forest.class})
class CephalidVandalTest extends BaseCardTest {

    @Test
    @DisplayName("The first upkeep adds a shred counter and mills one card")
    void firstUpkeepAddsCounterAndMillsOne() {
        Permanent vandal = harness.addToBattlefieldAndReturn(player1, new CephalidVandal());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(vandal.getCounterCount(CounterType.SHRED)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The mill count includes the shred counter added that upkeep")
    void millsForAllShredCounters() {
        Permanent vandal = harness.addToBattlefieldAndReturn(player1, new CephalidVandal());
        vandal.setCounterCount(CounterType.SHRED, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(vandal.getCounterCount(CounterType.SHRED)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}
