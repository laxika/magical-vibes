package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalakirCullblade.class, CruelEdict.class, GrizzlyBears.class})
class MalakirCullbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's creature dying puts a +1/+1 counter on the Cullblade")
    void gainsCounterWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new MalakirCullblade());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(findCullblade().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities(); // resolve the Cullblade trigger

        Permanent cullblade = findCullblade();
        assertThat(cullblade.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when the controller's own creature dies")
    void doesNotTriggerOnOwnCreatureDeath() {
        harness.addToBattlefield(player1, new MalakirCullblade());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(findCullblade().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters are permanent and accumulate across deaths")
    void countersAccumulateAndPersist() {
        harness.addToBattlefield(player1, new MalakirCullblade());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict(), new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // A second lone creature dies the same way, so the sacrifice choice stays automatic.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findCullblade().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.passBothPriorities(); // CLEANUP -> next turn

        assertThat(findCullblade().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Cullblade triggers independently for an opponent's creature death")
    void eachCullbladeGetsItsOwnCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MalakirCullblade());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MalakirCullblade());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent findCullblade() {
        return findPermanent(player1, "Malakir Cullblade");
    }
}
