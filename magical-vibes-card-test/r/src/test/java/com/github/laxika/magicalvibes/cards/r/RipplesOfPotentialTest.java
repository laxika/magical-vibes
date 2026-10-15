package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RipplesOfPotential.class, GrizzlyBears.class})
class RipplesOfPotentialTest extends BaseCardTest {

    @Test
    @DisplayName("Proliferates, then phases out chosen controlled permanents that received counters")
    void proliferatesThenPhasesOutChosenControlledPermanents() {
        Permanent ownReceived = addCounteredBear(player1);
        Permanent ownNotReceived = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentReceived = addCounteredBear(player2);

        cast();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice proliferateChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(proliferateChoice.validIds())
                .contains(ownReceived.getId(), opponentReceived.getId())
                .doesNotContain(ownNotReceived.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(ownReceived.getId(), opponentReceived.getId()));

        PendingInteraction.MultiPermanentChoice phaseOutChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(phaseOutChoice.validIds()).containsExactly(ownReceived.getId());
        assertThat(phaseOutChoice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, List.of(ownReceived.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(ownReceived);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ownNotReceived)
                .doesNotContain(ownReceived);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentReceived);
        assertThat(ownReceived.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentReceived.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The phase-out choice can choose none")
    void phaseOutChoiceCanChooseNone() {
        Permanent ownReceived = addCounteredBear(player1);

        cast();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownReceived.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownReceived);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of()))
                .doesNotContain(ownReceived);
        assertThat(ownReceived.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void choosingNoPermanentsToProliferateSkipsPhasing() {
        Permanent bear = addCounteredBear(player1);

        cast();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canPhaseOutOnlyASubsetOfPermanentsThatReceivedCounters() {
        Permanent chosen = addCounteredBear(player1);
        Permanent unchosen = addCounteredBear(player1);

        cast();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId(), unchosen.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unchosen).doesNotContain(chosen);
        assertThat(unchosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void phasesOutBeforeZeroToughnessStateBasedAction() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        cast();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bear.getCard());
        assertThat(bear.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    private Permanent addCounteredBear(com.github.laxika.magicalvibes.model.Player player) {
        Permanent bear = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return bear;
    }

    private void cast() {
        harness.castFromHand(player1, new RipplesOfPotential(), "{1}{U}");
    }
}
