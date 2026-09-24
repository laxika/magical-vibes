package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnointerOfValor.class, GrizzlyBears.class})
class AnointerOfValorTest extends BaseCardTest {

    @Test
    void paysManaToPutCounterOnAttackingCreature() {
        addCreatureReady(player1, new AnointerOfValor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void decliningPaymentDoesNotPutCounterOnAttacker() {
        addCreatureReady(player1, new AnointerOfValor());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        // Declining auto-passes into the next combat step, which empties the mana pool.
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void triggersForOpponentsAttackingCreature() {
        addCreatureReady(player1, new AnointerOfValor());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggersOnceForEachAttackingCreature() {
        addCreatureReady(player1, new AnointerOfValor());
        Permanent attacker1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker2 = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(attacker1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(attacker2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
