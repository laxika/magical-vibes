package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgentBishopManInBlack.class, ActionNewsCrew.class})
class AgentBishopManInBlackTest extends BaseCardTest {

    @Test
    void putsCountersOnUpToTwoTargetCreatures() {
        harness.addToBattlefield(player1, new AgentBishopManInBlack());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ActionNewsCrew());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ActionNewsCrew());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayChooseNoCreatures() {
        harness.addToBattlefield(player1, new AgentBishopManInBlack());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ActionNewsCrew());

        advanceToCombat(player1);

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new AgentBishopManInBlack());
        harness.addToBattlefield(player1, new ActionNewsCrew());

        advanceToCombat(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayChooseOnlyItself() {
        Permanent bishop = harness.addToBattlefieldAndReturn(player1, new AgentBishopManInBlack());

        advanceToCombat(player1);

        harness.handlePermanentChosen(player1, bishop.getId());
        harness.passBothPriorities();

        assertThat(bishop.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void stillPutsCounterOnRemainingTargetWhenOtherTargetLeaves() {
        harness.addToBattlefield(player1, new AgentBishopManInBlack());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ActionNewsCrew());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ActionNewsCrew());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void resolvesAfterBishopLeavesBattlefield() {
        Permanent bishop = harness.addToBattlefieldAndReturn(player1, new AgentBishopManInBlack());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ActionNewsCrew());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bishop);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
