package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GuardianKirin.class, GrizzlyBears.class, Shock.class})
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
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

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

    private void killCreature(Player controller) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(controller, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
