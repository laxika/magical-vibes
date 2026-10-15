package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BubblingMuck;
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

        int lifeBefore = gd.getLife(player2.getId());
        harness.castFromHand(player2, new BubblingMuck(), "{B}");
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void usesCurrentPowerWhenTheTriggerResolves() {
        setUpOpponentTurn();
        Permanent arsonist = findPermanent(player1, "Gleeful Arsonist");
        int lifeBefore = gd.getLife(player2.getId());
        harness.castFromHand(player2, new BubblingMuck(), "{B}");
        arsonist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void doesNotTriggerForCreatureSpells() {
        setUpOpponentTurn();

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

    @Test
    void doesNotTriggerForItsControllersNoncreatureSpell() {
        harness.addToBattlefield(player1, new GleefulArsonist());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castFromHand(player1, new BubblingMuck(), "{B}");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void pendingTriggerUsesOldPowerAfterUndyingReturnsTheSource() {
        setUpOpponentTurn();
        Permanent arsonist = findPermanent(player1, "Gleeful Arsonist");
        int lifeBefore = gd.getLife(player2.getId());
        harness.castFromHand(player2, new BubblingMuck(), "{B}");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, arsonist.getId());
        resolveAllTriggers();

        Permanent returnedArsonist = findPermanent(player1, "Gleeful Arsonist");
        assertThat(returnedArsonist.getId()).isNotEqualTo(arsonist.getId());
        assertThat(returnedArsonist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    void undyingDoesNotReturnItWhenItDiesWithAPlusOnePlusOneCounter() {
        Permanent arsonist = harness.addToBattlefieldAndReturn(player1, new GleefulArsonist());
        arsonist.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, arsonist.getId());
        resolveAllTriggers();
        harness.castInstant(player2, 0, arsonist.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gleeful Arsonist");
        harness.assertInGraveyard(player1, "Gleeful Arsonist");
    }

    @Test
    void undyingTriggerIsControlledByTheControllerAtDeathRatherThanTheOwner() {
        GleefulArsonist card = new GleefulArsonist();
        card.setOwnerId(player1.getId());
        Permanent arsonist = harness.addToBattlefieldAndReturn(player2, card);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, arsonist.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Gleeful Arsonist");
        harness.assertNotOnBattlefield(player2, "Gleeful Arsonist");
    }

    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new GleefulArsonist());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
