package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuirionBeastcaller.class, GrizzlyBears.class, HillGiant.class,
        Assassinate.class, Shock.class})
class QuirionBeastcallerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when you cast a creature spell")
    void getsCounterWhenCreatureSpellIsCast() {
        Permanent beastcaller = addCreatureReady(player1, new QuirionBeastcaller());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(beastcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when you cast a noncreature spell")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent beastcaller = addCreatureReady(player1, new QuirionBeastcaller());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(beastcaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("On death, distributes its counters among controlled creatures")
    void deathDistributesCountersAmongControlledCreatures() {
        Permanent beastcaller = addCreatureReady(player1, new QuirionBeastcaller());
        beastcaller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        beastcaller.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 2, giant.getId(), 1);

        killBeastcaller(beastcaller);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death distribution can be declined")
    void deathDistributionCanBeDeclined() {
        Permanent beastcaller = addCreatureReady(player1, new QuirionBeastcaller());
        beastcaller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        beastcaller.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        gd.pendingETBDamageAssignments = Map.of(bears.getId(), 3);

        killBeastcaller(beastcaller);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Death distribution ignores opponent creatures")
    void deathDistributionIgnoresOpponentCreatures() {
        Permanent beastcaller = addCreatureReady(player1, new QuirionBeastcaller());
        beastcaller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        beastcaller.tap();
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        gd.pendingETBDamageAssignments = Map.of(ownBears.getId(), 1, opponentGiant.getId(), 2);

        killBeastcaller(beastcaller);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ownBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void killBeastcaller(Permanent beastcaller) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Assassinate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID beastcallerId = beastcaller.getId();
        gs.playCard(gd, player1, 0, 0, beastcallerId, null);
        harness.passBothPriorities();
    }
}
