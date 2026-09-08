package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomNishoba.class, GrizzlyBears.class, Shock.class})
class PhantomNishobaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with seven +1/+1 counters")
    void entersWithSevenCounters() {
        harness.setHand(player1, List.of(new PhantomNishoba()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent nishoba = findNishoba(player1);
        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("Damage to it is prevented and removes one counter per damage")
    void damageIsPreventedAndRemovesCounters() {
        harness.addToBattlefield(player2, new PhantomNishoba());
        Permanent nishoba = findNishoba(player2);
        nishoba.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, nishoba.getId());
        harness.passBothPriorities();

        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(nishoba.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Gains life equal to damage dealt in combat")
    void gainsLifeEqualToDamageDealt() {
        Permanent nishoba = addAttacker(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(27);
        assertThat(nishoba.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent nishoba = addCreatureReady(player, new PhantomNishoba());
        nishoba.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 7);
        nishoba.setAttacking(true);
        return nishoba;
    }

    private Permanent findNishoba(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof PhantomNishoba)
                .findFirst()
                .orElseThrow();
    }
}
