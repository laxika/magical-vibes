package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AerithGainsborough.class, AjanisWelcome.class, ArvadTheCursed.class,
        Assassinate.class, GrizzlyBears.class})
class AerithGainsboroughTest extends BaseCardTest {

    @Test
    @DisplayName("Gains a +1/+1 counter whenever you gain life")
    void gainsCounterOnLifeGain() {
        Permanent aerith = addCreatureReady(player1, new AerithGainsborough());
        harness.addToBattlefield(player1, new AjanisWelcome());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(aerith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("On death, puts its counters on each legendary creature you control")
    void deathPutsCountersOnControlledLegendaryCreatures() {
        Permanent aerith = addCreatureReady(player1, new AerithGainsborough());
        aerith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        aerith.tap();
        Permanent ownLegendary = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        Permanent ownNonlegendary = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLegendary = harness.addToBattlefieldAndReturn(player2, new ArvadTheCursed());

        destroyWithAssassinate(aerith);

        harness.passBothPriorities();

        assertThat(ownLegendary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownNonlegendary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentLegendary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lifelink gains two life but adds only one counter per life-gain event")
    void lifelinkAddsOneCounterForMultipleLife() {
        Permanent aerith = addCreatureReady(player1, new AerithGainsborough());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        aerith.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(aerith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent gaining life does not put a counter on Aerith")
    void opponentLifeGainDoesNotAddCounter() {
        Permanent aerith = addCreatureReady(player1, new AerithGainsborough());
        harness.addToBattlefield(player2, new AjanisWelcome());
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
        assertThat(aerith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Dying with no counters adds no counters to legendary creatures")
    void deathWithZeroCountersAddsNone() {
        Permanent aerith = addCreatureReady(player1, new AerithGainsborough());
        aerith.tap();
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        legendary.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        destroyWithAssassinate(aerith);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aerith.getCard());
        assertThat(legendary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death counts only +1/+1 counters and affects creatures present at resolution")
    void deathUsesPlusOneCountersAndResolutionBattlefield() {
        Permanent aerith = addCreatureReady(player1, new AerithGainsborough());
        aerith.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        aerith.setCounterCount(CounterType.CHARGE, 3);
        aerith.tap();

        destroyWithAssassinate(aerith);
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        legendary.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(legendary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private void destroyWithAssassinate(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player2, 0, target.getId());
    }
}
