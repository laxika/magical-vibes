package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.Gravecrawler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaywardServantTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 and controller gains 1 when another Zombie enters")
    void drainsWhenAnotherZombieEnters() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new WaywardServant());

        // Cast Gravecrawler (a Zombie) under player1's control.
        harness.setHand(player1, List.of(new Gravecrawler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
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

        // Cast Grizzly Bears (a Bear, not a Zombie).
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
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
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Gravecrawler()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
