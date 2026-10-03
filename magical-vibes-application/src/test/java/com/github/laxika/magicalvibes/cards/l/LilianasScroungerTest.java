package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LilianasScrounger.class, LilianaWakerOfTheDead.class})
class LilianasScroungerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger at an end step when no creature died this turn")
    void doesNotTriggerWithoutMorbid() {
        harness.addToBattlefield(player1, new LilianasScrounger());
        addReadyLiliana(player1, 3);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("At any player's end step, may put a loyalty counter on a Liliana planeswalker you control")
    void addsLoyaltyAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new LilianasScrounger());
        Permanent liliana = addReadyLiliana(player1, 3);
        addReadyLiliana(player2, 5);
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, liliana.getId());
        }

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(findPermanent(player2, "Liliana, Waker of the Dead")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("The loyalty counter may be declined")
    void mayDeclineLoyaltyCounter() {
        harness.addToBattlefield(player1, new LilianasScrounger());
        Permanent liliana = addReadyLiliana(player1, 3);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    private Permanent addReadyLiliana(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LilianaWakerOfTheDead());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
