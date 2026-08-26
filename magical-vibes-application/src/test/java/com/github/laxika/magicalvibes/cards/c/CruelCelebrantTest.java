package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AjaniGoldmane;
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

@CardUsed({CruelCelebrant.class, AjaniGoldmane.class, GrizzlyBears.class, Shock.class})
class CruelCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("When Cruel Celebrant dies, each opponent loses 1 life and its controller gains 1 life")
    void selfDeathTriggers() {
        harness.addToBattlefield(player1, new CruelCelebrant());
        setStartingLifeTotals();

        killWithShock(player2, player1, "Cruel Celebrant");

        assertLifeAfterDeath(player1, player2);
    }

    @Test
    @DisplayName("When another creature you control dies, each opponent loses 1 life and you gain 1 life")
    void allyCreatureDeathTriggers() {
        harness.addToBattlefield(player1, new CruelCelebrant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        setStartingLifeTotals();

        killWithShock(player2, player1, "Grizzly Bears");

        assertLifeAfterDeath(player1, player2);
    }

    @Test
    @DisplayName("When a planeswalker you control dies, each opponent loses 1 life and you gain 1 life")
    void allyPlaneswalkerDeathTriggers() {
        harness.addToBattlefield(player1, new CruelCelebrant());
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniGoldmane());
        ajani.setCounterCount(CounterType.LOYALTY, 0);
        ajani.setSummoningSick(false);
        setStartingLifeTotals();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertLifeAfterDeath(player1, player2);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger Cruel Celebrant")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new CruelCelebrant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setStartingLifeTotals();

        killWithShock(player1, player2, "Grizzly Bears");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster,
                               com.github.laxika.magicalvibes.model.Player targetPlayer,
                               String targetName) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, harness.getPermanentId(targetPlayer, targetName));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setStartingLifeTotals() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
    }

    private void assertLifeAfterDeath(com.github.laxika.magicalvibes.model.Player celebrantController,
                                      com.github.laxika.magicalvibes.model.Player opponent) {
        harness.assertLife(celebrantController, 21);
        harness.assertLife(opponent, 19);
    }
}
