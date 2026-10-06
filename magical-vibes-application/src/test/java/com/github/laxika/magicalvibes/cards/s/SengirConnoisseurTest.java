package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.ExtinguishTheLight;
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

@CardUsed({SengirConnoisseur.class, GrizzlyBears.class, Shock.class, ExtinguishTheLight.class})
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
        harness.castAndResolveInstant(player2, 0, victim.getId());
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

        harness.castAndResolveInstant(player2, 0, firstVictim.getId());
        harness.passBothPriorities();

        // The remaining Bear is the only legal creature with that name after the first death.
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));

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
        harness.castAndResolveInstant(player2, 0, firstVictim.getId());
        harness.passBothPriorities();

        assertThat(connoisseur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        advanceTurn();
        advanceTurn();

        Permanent secondVictim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, secondVictim.getId());
        harness.passBothPriorities();

        assertThat(connoisseur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for its own death")
    void doesNotTriggerForOwnDeath() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new SengirConnoisseur());
        harness.setHand(player2, List.of(new ExtinguishTheLight()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, connoisseur.getId());

        harness.assertInGraveyard(player1, "Sengir Connoisseur");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers when another creature controlled by its controller dies")
    void triggersForAnotherControlledCreature() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new SengirConnoisseur());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new SengirConnoisseur());
        harness.setHand(player2, List.of(new ExtinguishTheLight()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player2, 0, victim.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(connoisseur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second death before the first trigger resolves does not trigger again")
    void secondDeathWhileTriggerPendingDoesNotTriggerAgain() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new SengirConnoisseur());
        Permanent firstVictim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondVictim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, firstVictim.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, secondVictim.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(connoisseur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A pending trigger does not put a counter on a different copy after its source dies")
    void pendingTriggerDoesNotAffectDifferentCopy() {
        Permanent connoisseur = harness.addToBattlefieldAndReturn(player1, new SengirConnoisseur());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new SengirConnoisseur());
        harness.setHand(player2, List.of(new ExtinguishTheLight(), new ExtinguishTheLight()));
        harness.addMana(player2, ManaColor.BLACK, 8);

        harness.castAndResolveInstant(player2, 0, victim.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, connoisseur.getId());
        assertThat(gd.stack).hasSize(1);
        Permanent otherCopy = harness.addToBattlefieldAndReturn(player1, new SengirConnoisseur());
        harness.passBothPriorities();

        assertThat(otherCopy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
