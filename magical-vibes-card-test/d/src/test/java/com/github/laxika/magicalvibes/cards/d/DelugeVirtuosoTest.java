package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
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

@CardUsed({DelugeVirtuoso.class, GrizzlyBears.class, Hurricane.class, Shock.class})
class DelugeVirtuosoTest extends BaseCardTest {

    private Permanent addReadyVirtuoso(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DelugeVirtuoso());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("ETB taps target opponent creature and puts a stun counter on it")
    void etbTapsAndStunsTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(bears.isTapped()).isFalse();
        UUID targetId = bears.getId();

        harness.setHand(player1, List.of(new DelugeVirtuoso()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a one-mana instant gives +1/+1")
    void cheapSpellGivesPlusOne() {
        Permanent virtuoso = addReadyVirtuoso(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(virtuoso.getPowerModifier()).isEqualTo(1);
        assertThat(virtuoso.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a four-mana spell gives +1/+1 (below threshold)")
    void fourManaSpellGivesPlusOne() {
        Permanent virtuoso = addReadyVirtuoso(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(virtuoso.getPowerModifier()).isEqualTo(1);
        assertThat(virtuoso.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a five-mana spell gives +2/+2 instead")
    void fiveManaSpellGivesPlusTwo() {
        Permanent virtuoso = addReadyVirtuoso(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(virtuoso.getPowerModifier()).isEqualTo(2);
        assertThat(virtuoso.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the Opus boost")
    void creatureSpellDoesNotTrigger() {
        Permanent virtuoso = addReadyVirtuoso(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(virtuoso.getPowerModifier()).isZero();
        assertThat(virtuoso.getToughnessModifier()).isZero();
    }
    @Test
    @DisplayName("Two enters abilities put two stun counters on an already tapped creature")
    void repeatedEntriesAccumulateStunCounters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new DelugeVirtuoso(), new DelugeVirtuoso()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent casting an instant does not trigger Opus")
    void opponentSpellDoesNotTrigger() {
        Permanent virtuoso = addReadyVirtuoso(player1);
        setUpMainPhase(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(virtuoso.getPowerModifier()).isZero();
        assertThat(virtuoso.getToughnessModifier()).isZero();
        harness.assertLife(player1, 18);
    }
}
