package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Vulpikeet.class)
class VulpikeetTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating puts a +1/+1 counter on Vulpikeet")
    void mutatingPutsCounterOnIt() {
        Permanent vulpikeet = addCreatureReady(player1, new Vulpikeet());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, vulpikeet, List.of(vulpikeet.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(vulpikeet.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
