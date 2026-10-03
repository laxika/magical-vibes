package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JungleWeaver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CalderaHellion.class, GrizzlyBears.class, JungleWeaver.class})
class CalderaHellionTest extends BaseCardTest {

    private void castHellion() {
        harness.castFromHand(player1, new CalderaHellion(), "{3}{R}{R}");
    }

    @Test
    @DisplayName("With no other creatures, ETB deals 3 to each creature and the 3/3 Hellion dies")
    void etbKillsItselfAndOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        castHellion();
        harness.passBothPriorities(); // resolve creature spell (no devour prompt: no other creatures)
        harness.passBothPriorities(); // resolve ETB mass damage

        // Hellion (3/3) took 3 -> dies; opponent's 2/2 also dies.
        harness.assertNotOnBattlefield(player1, "Caldera Hellion");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
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

        Permanent hellion = findPermanent(player1, "Caldera Hellion");
        assertThat(hellion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities(); // resolve ETB mass damage

        // Hellion is now a 4/4 with 3 marked damage -> survives.
        hellion = findPermanent(player1, "Caldera Hellion");
        assertThat(hellion.getMarkedDamage()).isEqualTo(3);
        // Opponent's 2/2 took 3 -> dies.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Devouring nothing enters with no counters and the Hellion dies to its own ETB")
    void devourNoneNoCounters() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castHellion();
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        Permanent hellion = findPermanent(player1, "Caldera Hellion");
        assertThat(hellion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities(); // resolve ETB mass damage

        // 3/3 with no counters dies to its own 3 damage; the other 2/2 dies too.
        harness.assertNotOnBattlefield(player1, "Caldera Hellion");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Devour sacrifices multiple creatures before the damage trigger and leaves unchosen creatures")
    void devoursMultipleCreaturesBeforeDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new JungleWeaver());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new JungleWeaver());
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new JungleWeaver());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new JungleWeaver());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castHellion();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));

        Permanent hellion = findPermanent(player1, "Caldera Hellion");
        assertThat(hellion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard(), second.getCard());
        assertThat(hellion.getMarkedDamage()).isZero();
        assertThat(survivor.getMarkedDamage()).isZero();
        assertThat(opponent.getMarkedDamage()).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Caldera Hellion");
        harness.assertOnBattlefield(player1, "Jungle Weaver");
        harness.assertOnBattlefield(player2, "Jungle Weaver");
        assertThat(hellion.getMarkedDamage()).isEqualTo(3);
        assertThat(survivor.getMarkedDamage()).isEqualTo(3);
        assertThat(opponent.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
