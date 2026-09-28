package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BubblingMuck;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GleefulArsonist.class, BubblingMuck.class, GrizzlyBears.class, Shock.class})
class GleefulArsonistTest extends BaseCardTest {

    @Test
    void damagesOpponentForItsPowerWhenTheyCastNoncreatureSpell() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new BubblingMuck()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castFromHand(player2, new BubblingMuck(), "{B}");
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void usesCurrentPowerWhenTheTriggerResolves() {
        setUpOpponentTurn();
        Permanent arsonist = findPermanent(player1, "Gleeful Arsonist");
        arsonist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new BubblingMuck()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castFromHand(player2, new BubblingMuck(), "{B}");
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void doesNotTriggerForCreatureSpells() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player2.getId());
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void undyingReturnsItWithPlusOnePlusOneCounter() {
        Permanent arsonist = harness.addToBattlefieldAndReturn(player1, new GleefulArsonist());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, arsonist.getId());
        resolveAllTriggers();

        Permanent returnedArsonist = findPermanent(player1, "Gleeful Arsonist");
        assertThat(returnedArsonist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Gleeful Arsonist");
    }

    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new GleefulArsonist());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
