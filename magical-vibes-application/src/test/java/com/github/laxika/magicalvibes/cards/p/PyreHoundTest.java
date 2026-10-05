package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.m.MagmaticChasm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyreHound.class, FieryTemper.class, MagmaticChasm.class})
class PyreHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant puts a +1/+1 counter on Pyre Hound")
    void instantPutsCounter() {
        Permanent hound = addCreatureReady(player1, new PyreHound());
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery puts a +1/+1 counter on Pyre Hound")
    void sorceryPutsCounter() {
        Permanent hound = addCreatureReady(player1, new PyreHound());
        harness.setHand(player1, List.of(new MagmaticChasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not trigger Pyre Hound")
    void creatureDoesNotPutCounter() {
        Permanent hound = addCreatureReady(player1, new PyreHound());
        harness.setHand(player1, List.of(new PyreHound()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter trigger resolves before the instant that caused it")
    void counterResolvesBeforeSpell() {
        Permanent hound = addCreatureReady(player1, new PyreHound());
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, hound.getId());

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Pyre Hound");
        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Pyre Hound")
    void opponentSpellDoesNotPutCounter() {
        Permanent hound = addCreatureReady(player1, new PyreHound());
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(hound.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Each controlled Hound gets a counter for every qualifying cast")
    void multipleHoundsAndSpellsAccumulateCounters() {
        Permanent first = addCreatureReady(player1, new PyreHound());
        Permanent second = addCreatureReady(player1, new PyreHound());
        Permanent opposing = addCreatureReady(player2, new PyreHound());
        harness.setHand(player1, List.of(new FieryTemper(), new MagmaticChasm()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
