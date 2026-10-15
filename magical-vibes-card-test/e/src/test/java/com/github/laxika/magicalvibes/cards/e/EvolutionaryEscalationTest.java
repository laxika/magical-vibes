package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvolutionaryEscalation.class, GrizzlyBears.class})
class EvolutionaryEscalationTest extends BaseCardTest {

    @Test
    @DisplayName("Puts three +1/+1 counters on one creature on each side at upkeep")
    void putsCountersOnControlledAndOpponentCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new EvolutionaryEscalation());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when either required side has no creature")
    void doesNotTriggerWithoutAnOpponentCreature() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new EvolutionaryEscalation());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, ownCreature.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
