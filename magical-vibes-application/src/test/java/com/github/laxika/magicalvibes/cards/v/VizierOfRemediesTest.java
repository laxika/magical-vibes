package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.ContagionClasp;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.cards.s.SplendidAgony;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VizierOfRemedies.class, AirElemental.class, Skinrender.class, ContagionClasp.class,
        Colossapede.class, SplendidAgony.class})
class VizierOfRemediesTest extends BaseCardTest {

    private Permanent airElemental(com.github.laxika.magicalvibes.model.Player owner) {
        return findPermanent(owner, "Air Elemental");
    }

    @Test
    @DisplayName("Reduces -1/-1 counters put on a creature you control by one")
    void reducesCountersOnControlledCreature() {
        harness.addToBattlefield(player1, new VizierOfRemedies());
        harness.addToBattlefield(player1, new AirElemental());
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Skinrender would put three -1/-1 counters; Vizier reduces to two. 4/4 → 2/2.
        Permanent target = airElemental(player1);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not reduce -1/-1 counters on a creature an opponent controls")
    void doesNotReduceOnOpponentCreature() {
        harness.addToBattlefield(player1, new VizierOfRemedies());
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // The opponent doesn't control the Vizier, so all three counters land.
        assertThat(airElemental(player2).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A single -1/-1 counter is reduced to zero (none is placed)")
    void singleCounterReducedToZero() {
        harness.addToBattlefield(player1, new VizierOfRemedies());
        harness.addToBattlefield(player1, new AirElemental());
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.setHand(player1, List.of(new ContagionClasp()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castArtifact(player1, 0, targetId);
        harness.passBothPriorities(); // resolve artifact spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Contagion Clasp's single -1/-1 counter minus one = zero.
        Permanent target = airElemental(player1);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Viziers stack, reducing -1/-1 counters by two")
    void twoViziersStack() {
        harness.addToBattlefield(player1, new VizierOfRemedies());
        harness.addToBattlefield(player1, new VizierOfRemedies());
        harness.addToBattlefield(player1, new AirElemental());
        UUID targetId = harness.getPermanentId(player1, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Three counters minus two Viziers = one.
        assertThat(airElemental(player1).getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Protects itself while counters on an opposing creature are unaffected")
    void protectsItselfFromASingleCounter() {
        Permanent vizier = harness.addToBattlefieldAndReturn(player1, new VizierOfRemedies());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        harness.setHand(player1, List.of(new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, List.of(vizier.getId(), opposing.getId()));

        assertThat(vizier.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Vizier of Remedies");
        assertThat(opposing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Reduces the counters separately for each controlled creature in one spell")
    void protectsEachCreatureInTheSameEvent() {
        harness.addToBattlefield(player1, new VizierOfRemedies());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        harness.setHand(player1, List.of(new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Reduces each new placement without removing counters already present")
    void reducesEachSeparatePlacement() {
        harness.addToBattlefield(player1, new VizierOfRemedies());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Colossapede());
        harness.setHand(player1, List.of(new SplendidAgony(), new SplendidAgony()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }
}
