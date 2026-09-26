package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrapTheTrespassers.class, GrizzlyBears.class})
class TrapTheTrespassersTest extends BaseCardTest {

    @Test
    void putsOneStunCounterOnEachCreatureChosenByOnePlayerAndTapsThem() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTrap();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    void putsTwoStunCountersOnCreatureReceivingBothVotes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTrap();

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void automaticallyVotesWhenOnlyOneCreatureCanBeChosen() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castTrap();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(target.isTapped()).isTrue();
    }

    private void castTrap() {
        harness.setHand(player1, List.of(new TrapTheTrespassers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private PendingInteraction.MultiPermanentChoice activeVote() {
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        return choice;
    }
}
