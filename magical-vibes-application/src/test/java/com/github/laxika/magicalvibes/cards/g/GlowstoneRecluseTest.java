package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GlowstoneRecluse.class)
class GlowstoneRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating puts two +1/+1 counters on Glowstone Recluse")
    void mutatingPutsTwoCountersOnIt() {
        Permanent recluse = addCreatureReady(player1, new GlowstoneRecluse());

        mutate(recluse);

        assertThat(recluse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each mutation puts two more +1/+1 counters on Glowstone Recluse")
    void eachMutationPutsTwoMoreCountersOnIt() {
        Permanent recluse = addCreatureReady(player1, new GlowstoneRecluse());

        mutate(recluse);
        mutate(recluse);

        assertThat(recluse.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private void mutate(Permanent recluse) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, recluse, List.of(recluse.getCard()), player1.getId()));
        resolveAllTriggers();
    }
}
