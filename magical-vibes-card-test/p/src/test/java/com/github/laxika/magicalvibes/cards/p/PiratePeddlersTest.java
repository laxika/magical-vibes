package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DeadlyPrecision;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiratePeddlers.class, ZuranOrb.class, Forest.class, DeadlyPrecision.class})
class PiratePeddlersTest extends BaseCardTest {

    @Test
    @DisplayName("Putting another permanent into a graveyard by sacrificing it adds a +1/+1 counter")
    void gainsCounterWhenAnotherPermanentIsSacrificed() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new PiratePeddlers());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        prepareMainPhase(player1);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(pirate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("An opponent sacrificing a permanent does not add a counter")
    void doesNotTriggerForOpponentSacrifice() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new PiratePeddlers());
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.addToBattlefield(player2, new Forest());
        prepareMainPhase(player2);

        harness.activateAbility(player2, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(pirate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void gainsOneCounterForEachSacrificeInTheSameTurn() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new PiratePeddlers());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        prepareMainPhase(player1);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();
        assertThat(pirate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(pirate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Forest")).isZero();
    }

    @Test
    void sacrificingAnotherPiratePeddlersAddsCounterBeforeSpellResolves() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new PiratePeddlers());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new PiratePeddlers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PiratePeddlers());
        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareMainPhase(player1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        assertThat(pirate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(pirate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Pirate Peddlers");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Pirate Peddlers");
        assertThat(pirate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void sacrificingItselfDoesNotTriggerItsAbility() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new PiratePeddlers());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PiratePeddlers());
        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareMainPhase(player1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), pirate.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Pirate Peddlers");
        resolveAllTriggers();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void destroyingAnotherCreatureWithoutSacrificingDoesNotAddCounter() {
        Permanent pirate = harness.addToBattlefieldAndReturn(player1, new PiratePeddlers());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PiratePeddlers());
        harness.setHand(player1, List.of(new DeadlyPrecision()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        prepareMainPhase(player1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), null);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Pirate Peddlers")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Pirate Peddlers");
        assertThat(pirate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
