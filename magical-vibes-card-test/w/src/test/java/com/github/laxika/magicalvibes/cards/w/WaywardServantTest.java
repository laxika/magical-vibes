package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.f.FesteringMummy;
import com.github.laxika.magicalvibes.cards.l.LilianasMastery;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaywardServant.class, FesteringMummy.class, Colossapede.class, LilianasMastery.class})
class WaywardServantTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 and controller gains 1 when another Zombie enters")
    void drainsWhenAnotherZombieEnters() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WaywardServant());

        harness.castFromHand(player1, new FesteringMummy(), "{B}");
        harness.passBothPriorities(); // resolve creature spell (Zombie enters, triggers Wayward Servant)
        harness.passBothPriorities(); // resolve the drain trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not trigger when a non-Zombie creature enters")
    void noTriggerWhenNonZombieEnters() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WaywardServant());

        harness.castFromHand(player1, new Colossapede(), "{4}{G}");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when an opponent's Zombie enters")
    void noTriggerWhenOpponentZombieEnters() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WaywardServant());

        // Opponent casts a Zombie.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new FesteringMummy(), "{B}");
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger for its own entry")
    void noTriggerWhenServantItselfEnters() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new WaywardServant(), "{W}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A second Servant triggers the first once, with loss and gain in one ability")
    void secondServantCreatesOneDrainTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WaywardServant());

        harness.castFromHand(player1, new WaywardServant(), "{W}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Each Zombie token in a simultaneous entry creates its own drain trigger")
    void drainsForEachZombieToken() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WaywardServant());

        harness.castFromHand(player1, new LilianasMastery(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
