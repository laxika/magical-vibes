package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TransguildCourier;
import com.github.laxika.magicalvibes.cards.t.Tatterkite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SimicInitiate.class, TransguildCourier.class, Tatterkite.class})
class SimicInitiateTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithOneCounter() {
        Permanent initiate = castInitiate();

        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft moves its +1/+1 counter onto another creature that enters")
    void graftMovesCounterOntoEnteringCreature() {
        Permanent initiate = castInitiate();

        harness.castFromHand(player1, new TransguildCourier(), "{4}");
        harness.passBothPriorities();
        Permanent courier = findPermanent(player1, "Transguild Courier");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(initiate);
        assertThat(courier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may move a counter onto an opponent's creature")
    void graftMovesCounterOntoOpponentsCreature() {
        Permanent initiate = castInitiate();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new TransguildCourier(), "{4}");
        harness.passBothPriorities();
        Permanent courier = findPermanent(player2, "Transguild Courier");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(initiate);
        assertThat(courier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft may be declined")
    void graftMayBeDeclined() {
        Permanent initiate = castInitiate();

        harness.castFromHand(player1, new TransguildCourier(), "{4}");
        harness.passBothPriorities();
        Permanent courier = findPermanent(player1, "Transguild Courier");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(initiate);
        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(courier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Graft does not trigger for its own entry")
    void graftDoesNotTriggerForItsOwnEntry() {
        castInitiate();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Graft moves only one counter when the source has several")
    void graftMovesOnlyOneCounter() {
        Permanent initiate = castInitiate();
        initiate.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.castFromHand(player1, new TransguildCourier(), "{4}");
        harness.passBothPriorities();
        Permanent courier = findPermanent(player1, "Transguild Courier");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(courier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(initiate);
    }

    @Test
    @DisplayName("Graft retains its counter when the entering creature cannot receive counters")
    void graftCannotMoveCounterOntoTatterkite() {
        Permanent initiate = castInitiate();

        harness.castFromHand(player1, new Tatterkite(), "{3}");
        harness.passBothPriorities();
        Permanent tatterkite = findPermanent(player1, "Tatterkite");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(initiate);
        assertThat(initiate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tatterkite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Graft goes on the stack and its controller chooses as it resolves (CR 603.5)")
    void graftGoesOnTheStackAndAsksAsItResolves() {
        Permanent initiate = castInitiate();

        harness.castFromHand(player1, new TransguildCourier(), "{4}");
        harness.passBothPriorities();
        Permanent courier = findPermanent(player1, "Transguild Courier");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(initiate);
        assertThat(courier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castInitiate() {
        harness.castFromHand(player1, new SimicInitiate(), "{G}");
        harness.passBothPriorities();
        return findPermanent(player1, "Simic Initiate");
    }
}
