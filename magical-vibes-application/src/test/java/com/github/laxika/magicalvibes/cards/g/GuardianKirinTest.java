package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuardianKirin.class, GrizzlyBears.class, Shock.class, Pyroclasm.class})
class GuardianKirinTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when another creature you control dies")
    void getsCounterWhenAllyCreatureDies() {
        harness.addToBattlefield(player1, new GuardianKirin());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent guardianKirin = findPermanent(player1, "Guardian Kirin");
        killCreature(player1);

        assertThat(guardianKirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when an opponent's creature dies")
    void doesNotGetCounterWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new GuardianKirin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent guardianKirin = findPermanent(player1, "Guardian Kirin");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsId);

        assertThat(guardianKirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets one counter for each ally creature that dies")
    void accumulatesCounters() {
        harness.addToBattlefield(player1, new GuardianKirin());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent guardianKirin = findPermanent(player1, "Guardian Kirin");

        killCreature(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        killCreature(player1);

        assertThat(guardianKirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counter is placed only when the death trigger resolves")
    void counterWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new GuardianKirin());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent guardianKirin = findPermanent(player1, "Guardian Kirin");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(guardianKirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(guardianKirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Guardian Kirin does not trigger for its own death")
    void doesNotTriggerForOwnDeath() {
        harness.addToBattlefield(player1, new GuardianKirin());
        UUID kirinId = harness.getPermanentId(player1, "Guardian Kirin");
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, kirinId);
        harness.castAndResolveInstant(player1, 0, kirinId);

        harness.assertNotOnBattlefield(player1, "Guardian Kirin");
        harness.assertInGraveyard(player1, "Guardian Kirin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous deaths produce one trigger per other allied creature")
    void simultaneousDeathsProduceSeparateTriggers() {
        harness.addToBattlefield(player1, new GuardianKirin());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent guardianKirin = findPermanent(player1, "Guardian Kirin");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).hasSize(2);
        assertThat(guardianKirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(guardianKirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(guardianKirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    private void killCreature(Player controller) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(controller, "Grizzly Bears");
        harness.castAndResolveInstant(player2, 0, bearsId);
        harness.passBothPriorities();
    }
}
