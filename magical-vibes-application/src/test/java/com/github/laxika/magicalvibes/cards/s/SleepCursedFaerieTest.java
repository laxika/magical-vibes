package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SleepCursedFaerie.class})
class SleepCursedFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with three stun counters")
    void entersTappedWithThreeStunCounters() {
        harness.setHand(player1, List.of(new SleepCursedFaerie()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent faerie = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(faerie.isTapped()).isTrue();
        assertThat(faerie.getCounterCount(CounterType.STUN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Untap ability removes stun counters before untapping")
    void untapAbilityRemovesStunCountersBeforeUntapping() {
        Permanent faerie = harness.enterBattlefieldAndReturn(player1, new SleepCursedFaerie());
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, 0, 0, null, null);
            harness.passBothPriorities();
            assertThat(faerie.isTapped()).isTrue();
            assertThat(faerie.getCounterCount(CounterType.STUN)).isEqualTo(2 - i);
        }

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(faerie.isTapped()).isFalse();
        assertThat(faerie.getCounterCount(CounterType.STUN)).isZero();
    }
}
