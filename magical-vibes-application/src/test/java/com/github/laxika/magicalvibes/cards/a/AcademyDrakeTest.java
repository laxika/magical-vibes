package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcademyDrake.class})
class AcademyDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering without being cast does not grant kicker counters")
    void entersWithoutCastingHasNoCounters() {
        Permanent drake = harness.enterBattlefieldAndReturn(player1, new AcademyDrake());

        harness.assertOnBattlefield(player1, "Academy Drake");
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast without kicker — enters as 2/2 with no counters")
    void castWithoutKicker() {
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 2); // 2 generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Academy Drake");
        Permanent drake = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cast with kicker — enters as 4/4 with two +1/+1 counters")
    void castWithKicker() {
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6); // 2 generic + 4 kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Academy Drake");
        Permanent drake = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast with kicker but not enough mana — throws exception")
    void castWithKickerNotEnoughMana() {
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 3); // only 2 generic + 1 kicker (need 4)

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Having enough mana for kicker does not kick the spell automatically")
    void castWithoutKickerWithEnoughManaForKicker() {
        harness.setHand(player1, List.of(new AcademyDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Academy Drake");
        Permanent drake = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
