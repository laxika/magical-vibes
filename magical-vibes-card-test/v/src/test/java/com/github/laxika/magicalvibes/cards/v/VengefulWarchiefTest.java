package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({VengefulWarchief.class, Shock.class, VilisBrokerOfBlood.class})
class VengefulWarchiefTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gets a +1/+1 counter the first time its controller loses life each turn")
    void getsCounterOnFirstLifeLossEachTurn() {
        harness.addToBattlefield(player1, new VengefulWarchief());
        Permanent warchief = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(warchief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(warchief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers again on the first life loss of a later turn")
    void getsCounterAgainOnLaterTurn() {
        harness.addToBattlefield(player1, new VengefulWarchief());
        Permanent warchief = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        advanceTurn();
        advanceTurn();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(warchief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger if its controller already lost life before it entered")
    void doesNotTriggerAfterEarlierLifeLossBeforeEntry() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.addToBattlefield(player1, new VengefulWarchief());
        Permanent warchief = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castAndResolveInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        assertThat(warchief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's life loss does not consume the controller's first life loss")
    void opponentLifeLossDoesNotTriggerOrConsumeFirstLifeLoss() {
        harness.addToBattlefield(player1, new VengefulWarchief());
        Permanent warchief = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(warchief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player1, 0, player1.getId());
        resolveAllTriggers();
        assertThat(warchief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Warchief gets one counter from the same first life loss")
    void multipleWarchiefsTriggerIndependently() {
        harness.addToBattlefield(player1, new VengefulWarchief());
        harness.addToBattlefield(player1, new VengefulWarchief());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).allSatisfy(
                permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    @Test
    @DisplayName("Paying life triggers the counter before the activated ability resolves")
    void payingLifeTriggersBeforeAbilityResolves() {
        Permanent warchief = harness.addToBattlefieldAndReturn(player1, new VengefulWarchief());
        Permanent vilis = harness.addToBattlefieldAndReturn(player1, new VilisBrokerOfBlood());
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 1, null, vilis.getId());

        harness.assertLife(player1, 18);
        assertThat(warchief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        while (gd.stack.size() > 1) {
            harness.passBothPriorities();
        }
        assertThat(warchief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }
}
