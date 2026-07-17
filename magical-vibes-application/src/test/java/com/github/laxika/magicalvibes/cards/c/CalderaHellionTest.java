package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalderaHellionTest extends BaseCardTest {

    private void castHellion() {
        harness.setHand(player1, new ArrayList<>(List.of(new CalderaHellion())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("With no other creatures, ETB deals 3 to each creature and the 3/3 Hellion dies")
    void etbKillsItselfAndOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        castHellion();
        harness.passBothPriorities(); // resolve creature spell (no devour prompt: no other creatures)
        harness.passBothPriorities(); // resolve ETB mass damage

        // Hellion (3/3) took 3 -> dies; opponent's 2/2 also dies.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Caldera Hellion"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Devouring a creature adds a +1/+1 counter, letting the Hellion survive its own ETB damage")
    void devourAddsCounterAndSurvives() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 opponent

        castHellion();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodder.getId()));

        Permanent hellion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Caldera Hellion"))
                .findFirst().orElseThrow();
        assertThat(hellion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities(); // resolve ETB mass damage

        // Hellion is now a 4/4 with 3 marked damage -> survives.
        hellion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Caldera Hellion"))
                .findFirst().orElseThrow();
        assertThat(hellion.getMarkedDamage()).isEqualTo(3);
        // Opponent's 2/2 took 3 -> dies.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters and the Hellion dies to its own ETB")
    void devourNoneNoCounters() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHellion();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent hellion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Caldera Hellion"))
                .findFirst().orElseThrow();
        assertThat(hellion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities(); // resolve ETB mass damage

        // 3/3 with no counters dies to its own 3 damage; the other 2/2 dies too.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Caldera Hellion"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
