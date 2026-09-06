package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EssenceSymbiote.class, GrizzlyBears.class})
class EssenceSymbioteTest extends BaseCardTest {

    @Test
    @DisplayName("A controlled creature mutating gets a counter and its controller gains 2 life")
    void controlledCreatureMutatingGetsCounterAndLifeGain() {
        Permanent symbiote = addCreatureReady(player1, new EssenceSymbiote());
        Permanent mutatedCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        mutate(mutatedCreature, player1.getId());

        assertThat(mutatedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(symbiote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("An opponent's creature mutating does not trigger Essence Symbiote")
    void opponentCreatureMutatingDoesNotTrigger() {
        Permanent symbiote = addCreatureReady(player1, new EssenceSymbiote());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        mutate(opposingCreature, player2.getId());

        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(symbiote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
    }

    private void mutate(Permanent creature, java.util.UUID controllerId) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, creature, List.of(creature.getCard()), controllerId));
        resolveAllTriggers();
    }
}
