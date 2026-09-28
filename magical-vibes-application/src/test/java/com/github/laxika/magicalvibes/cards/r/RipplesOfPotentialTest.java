package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

    private Permanent addCounteredBear(com.github.laxika.magicalvibes.model.Player player) {
        Permanent bear = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return bear;
    }

    private void cast() {
        harness.setHand(player1, List.of(new RipplesOfPotential()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
    }
}
