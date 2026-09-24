package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SengirConnoisseur.class, GrizzlyBears.class, Shock.class})
class SengirConnoisseurTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when another creature dies")
    void putsCounterOnCreatureWhenAnotherCreatureDies() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new SengirConnoisseur());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, victim.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(connoisseur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers only once per turn")
    void triggersOnlyOncePerTurn() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new SengirConnoisseur());
        Permanent firstVictim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, firstVictim.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // The remaining Bear is the only legal creature with that name after the first death.
        harness.castInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(connoisseur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can trigger again on a later turn")
    void triggersAgainOnLaterTurn() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new SengirConnoisseur());
        Permanent firstVictim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, firstVictim.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(connoisseur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        advanceTurn();
        advanceTurn();

        Permanent secondVictim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, secondVictim.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(connoisseur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
