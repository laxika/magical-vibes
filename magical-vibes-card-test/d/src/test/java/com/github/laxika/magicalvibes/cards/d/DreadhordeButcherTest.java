package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.t.TibaltRakishInstigator;
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

@CardUsed({DreadhordeButcher.class, Murder.class, DomriAnarchOfBolas.class, TibaltRakishInstigator.class})
class DreadhordeButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamage() {
        Permanent butcher = addReadyButcher();
        harness.setLife(player2, 20);
        butcher.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();

        assertThat(butcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals damage equal to its power when it dies")
    void deathTriggerDealsDamageEqualToPower() {
        Permanent butcher = addReadyButcher();
        butcher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, butcher.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Dreadhorde Butcher");
    }

    @Test
    void getsCounterWhenDealingCombatDamageToPlaneswalker() {
        Permanent tibalt = harness.addToBattlefieldAndReturn(player2, new TibaltRakishInstigator());
        tibalt.setCounterCount(CounterType.LOYALTY, 5);
        Permanent butcher = addReadyButcher();
        butcher.setAttacking(true);
        butcher.setAttackTarget(tibalt.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        assertThat(tibalt.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.passBothPriorities();
        assertThat(butcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void deathTriggerCanDamageCreature() {
        Permanent butcher = addReadyButcher();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DreadhordeButcher());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, butcher.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Dreadhorde Butcher");
    }

    @Test
    void deathTriggerUsesPowerIncludingDomrisStaticBonus() {
        Permanent domri = harness.addToBattlefieldAndReturn(player1, new DomriAnarchOfBolas());
        domri.setCounterCount(CounterType.LOYALTY, 3);
        Permanent butcher = addReadyButcher();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, butcher.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Dreadhorde Butcher");
    }

    @Test
    void deathTriggerCanDamagePlaneswalker() {
        Permanent tibalt = harness.addToBattlefieldAndReturn(player2, new TibaltRakishInstigator());
        tibalt.setCounterCount(CounterType.LOYALTY, 5);
        Permanent butcher = addReadyButcher();
        butcher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, butcher.getId());
        harness.handlePermanentChosen(player1, tibalt.getId());
        harness.passBothPriorities();

        assertThat(tibalt.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyButcher() {
        Permanent butcher = harness.addToBattlefieldAndReturn(player1, new DreadhordeButcher());
        butcher.setSummoningSick(false);
        return butcher;
    }
}
