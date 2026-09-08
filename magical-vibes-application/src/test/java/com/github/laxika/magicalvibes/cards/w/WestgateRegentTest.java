package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WestgateRegent.class, GrizzlyBears.class, Shock.class})
class WestgateRegentTest extends BaseCardTest {

    @Test
    @DisplayName("Ward counters an opponent's spell when they have no card to discard")
    void wardCountersOpponentSpellWhenHandIsEmpty() {
        Permanent regent = addReadyWestgateRegent();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, regent.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Puts +1/+1 counters on itself equal to combat damage dealt to a player")
    void putsCountersEqualToCombatDamage() {
        Permanent regent = addReadyWestgateRegent();
        regent.setAttacking(true);

        harness.setLife(player2, 20);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not trigger when blocked and deals no combat damage to a player")
    void doesNotTriggerWhenBlocked() {
        Permanent regent = addReadyWestgateRegent();
        regent.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setLife(player2, 20);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(regent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyWestgateRegent() {
        Permanent regent = harness.addToBattlefieldAndReturn(player1, new WestgateRegent());
        regent.setSummoningSick(false);
        return regent;
    }
}
