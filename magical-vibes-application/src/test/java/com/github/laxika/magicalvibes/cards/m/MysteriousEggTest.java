package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MysteriousEgg.class)
class MysteriousEggTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating puts a +1/+1 counter on Mysterious Egg")
    void mutatingPutsCounterOnIt() {
        Permanent egg = addCreatureReady(player1, new MysteriousEgg());

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, egg, List.of(egg.getCard()), player1.getId()));
        resolveAllTriggers();

        assertThat(egg.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
