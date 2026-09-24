package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HulkGammaGoliath;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbominationIrradiatedBrute.class, HulkGammaGoliath.class, ArnimZolaBioFanatic.class, GrizzlyBears.class})
class AbominationIrradiatedBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters equal to combat damage from Gamma and Villain creatures")
    void putsCountersEqualToQualifyingCombatDamage() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new AbominationIrradiatedBrute());
        addAttacker(new HulkGammaGoliath());
        addAttacker(new ArnimZolaBioFanatic());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("Does not trigger for a creature that is neither Gamma nor Villain")
    void ignoresNonQualifyingCombatDamage() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new AbominationIrradiatedBrute());
        addAttacker(new GrizzlyBears());

        resolveCombat();
        resolveAllTriggers();

        assertThat(abomination.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
    }
}
