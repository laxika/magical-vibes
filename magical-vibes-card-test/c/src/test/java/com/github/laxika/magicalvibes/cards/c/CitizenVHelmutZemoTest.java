package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.d.DocOcksHenchmen;
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

@CardUsed({CitizenVHelmutZemo.class, DocOcksHenchmen.class, GrizzlyBears.class, AngelOfMercy.class})
class CitizenVHelmutZemoTest extends BaseCardTest {

    @Test
    @DisplayName("Puts +1/+1 counters on each Villain you control when you gain life")
    void putsCountersOnControlledVillains() {
        Permanent citizen = harness.addToBattlefieldAndReturn(player1, new CitizenVHelmutZemo());
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new DocOcksHenchmen());
        Permanent nonVillain = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentVillain = harness.addToBattlefieldAndReturn(player2, new DocOcksHenchmen());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(citizen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(villain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonVillain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentVillain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lifelink combat damage triggers one counter on each controlled Villain")
    void lifelinkTriggersCounters() {
        Permanent citizen = harness.addToBattlefieldAndReturn(player1, new CitizenVHelmutZemo());
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new DocOcksHenchmen());
        citizen.setSummoningSick(false);
        citizen.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(citizen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(villain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(citizen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(villain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent life gain does not put counters on your Villains")
    void opponentLifeGainDoesNotTrigger() {
        Permanent citizen = harness.addToBattlefieldAndReturn(player1, new CitizenVHelmutZemo());
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.setLife(player2, 20);

        harness.enterBattlefieldAndReturn(player2, new AngelOfMercy());
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        assertThat(gd.stack).isEmpty();
        assertThat(citizen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(villain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Separate life gains each give one counter regardless of the amount gained")
    void repeatedLifeGainsEachTrigger() {
        Permanent citizen = harness.addToBattlefieldAndReturn(player1, new CitizenVHelmutZemo());
        Permanent villain = harness.addToBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.setLife(player1, 20);

        for (int i = 0; i < 2; i++) {
            harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        harness.assertLife(player1, 26);
        assertThat(citizen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(villain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Villains entering after life gain get a counter when the trigger resolves")
    void determinesVillainsAtResolution() {
        Permanent citizen = harness.addToBattlefieldAndReturn(player1, new CitizenVHelmutZemo());
        harness.enterBattlefieldAndReturn(player1, new AngelOfMercy());
        harness.passBothPriorities();
        assertThat(citizen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent lateVillain = harness.enterBattlefieldAndReturn(player1, new DocOcksHenchmen());
        harness.passBothPriorities();

        assertThat(citizen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateVillain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
