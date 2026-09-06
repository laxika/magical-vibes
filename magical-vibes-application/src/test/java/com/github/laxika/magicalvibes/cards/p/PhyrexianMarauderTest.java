package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianMarauder.class, Warthog.class})
class PhyrexianMarauderTest extends BaseCardTest {

    private Permanent addReadyMarauder(Player controller, int counters) {
        Permanent marauder = addCreatureReady(controller, new PhyrexianMarauder());
        marauder.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return marauder;
    }

    @Test
    @DisplayName("Casting with X=3 enters with 3 +1/+1 counters")
    void entersWith3Counters() {
        harness.setHand(player1, List.of(new PhyrexianMarauder()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();

        Permanent marauder = findPermanent(player1, "Phyrexian Marauder");
        assertThat(marauder.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 enters as 0/0 and dies to state-based actions")
    void entersWith0CountersAndDies() {
        harness.setHand(player1, List.of(new PhyrexianMarauder()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Marauder");
    }

    @Test
    @DisplayName("Cannot be declared as a blocker")
    void cannotBlock() {
        addReadyMarauder(player2, 3);

        Permanent attacker = addCreatureReady(player1, new Warthog());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Attacks when controller pays {1} per +1/+1 counter")
    void attacksWhenPaid() {
        harness.setLife(player2, 20);
        addReadyMarauder(player1, 3);

        harness.addMana(player1, ManaColor.WHITE, 3);
        declareAttackers(List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot attack when controller cannot pay the per-counter tax")
    void cannotAttackWithoutPayment() {
        harness.setLife(player2, 20);
        addReadyMarauder(player1, 3);

        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @CardUsed(Humility.class)
    @DisplayName("Losing all abilities removes the per-counter attack tax")
    void losingAllAbilitiesRemovesAttackTax() {
        harness.addToBattlefield(player1, new Humility());
        addReadyMarauder(player1, 3);

        declareAttackers(List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
