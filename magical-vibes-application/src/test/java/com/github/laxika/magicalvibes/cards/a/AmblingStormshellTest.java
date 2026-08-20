package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmblingStormshellTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts three stun counters on Ambling Stormshell and draws three cards")
    void attackPutsStunCountersAndDrawsCards() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(shell.getCounterCount(CounterType.STUN)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Casting a Turtle spell untaps Ambling Stormshell")
    void turtleSpellUntapsShell() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        shell.tap();
        harness.setHand(player1, List.of(new HornedTurtle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(shell.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-Turtle spell does not untap Ambling Stormshell")
    void nonTurtleSpellDoesNotUntapShell() {
        Permanent shell = addCreatureReady(player1, new AmblingStormshell());
        shell.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(shell.isTapped()).isTrue();
    }
}
