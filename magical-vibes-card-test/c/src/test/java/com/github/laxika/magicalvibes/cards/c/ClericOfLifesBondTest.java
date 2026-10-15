package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ExpeditionDiviner;
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

@CardUsed({ClericOfLifesBond.class, ClericOfChillDepths.class, ExpeditionDiviner.class})
class ClericOfLifesBondTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when another Cleric you control enters")
    void gainsLifeWhenAnotherClericEnters() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ClericOfLifesBond());
        harness.setHand(player1, List.of(new ClericOfChillDepths()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not trigger when a non-Cleric enters")
    void doesNotTriggerForNonCleric() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ClericOfLifesBond());
        harness.enterBattlefieldAndReturn(player1, new ExpeditionDiviner());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on the first life gain each turn")
    void putsCounterOnFirstLifeGainEachTurn() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new ClericOfLifesBond());

        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        });
        harness.passBothPriorities();

        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotGainLifeForItsOwnEntry() {
        harness.setLife(player1, 20);
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, new ClericOfLifesBond());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForOpponentsClericOrLifeGain() {
        harness.setLife(player1, 20);
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new ClericOfLifesBond());
        harness.enterBattlefieldAndReturn(player2, new ClericOfChillDepths());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void clericEntriesContinueGainingLifeButOnlyFirstGainAddsCounter() {
        harness.setLife(player1, 20);
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new ClericOfLifesBond());
        harness.enterBattlefieldAndReturn(player1, new ClericOfChillDepths());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new ClericOfChillDepths());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerIfLifeWasAlreadyGainedBeforeItEntered() {
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, new ClericOfLifesBond());
        harness.enterBattlefieldAndReturn(player1, new ClericOfChillDepths());
        resolveAllTriggers();

        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void gainsOneCounterForLargeGainAndTriggersAgainOnOpponentsTurn() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new ClericOfLifesBond());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5));
        resolveAllTriggers();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2));
        resolveAllTriggers();

        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
