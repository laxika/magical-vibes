package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BalothGorger.class, BlinkOfAnEye.class})
class BalothGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering without being cast does not add kicker counters")
    void entersWithoutCastingHasNoCounters() {
        Permanent gorger = harness.enterBattlefieldAndReturn(player1, new BalothGorger());

        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast without kicker — enters as 4/4 with no counters")
    void castWithoutKicker() {
        harness.setHand(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2); // 2 generic

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent gorger = findGorger(player1);
        assertThat(gorger).isNotNull();
        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cast with kicker — enters as 7/7 with three +1/+1 counters")
    void castWithKicker() {
        harness.setHand(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 6); // 2 generic + 4 kicker

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent gorger = findGorger(player1);
        assertThat(gorger).isNotNull();
        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast with kicker but not enough mana — throws exception")
    void castWithKickerNotEnoughMana() {
        harness.setHand(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 3); // only 2 generic + 1 kicker (need 4)

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returning a kicked Gorger to hand and recasting without kicker removes its counters")
    void recastWithoutKickerHasNoCounters() {
        harness.setHand(player1, List.of(new BalothGorger(), new BlinkOfAnEye()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findGorger(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Baloth Gorger"));
        harness.assertNotOnBattlefield(player1, "Baloth Gorger");
        harness.assertInHand(player1, "Baloth Gorger");

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findGorger(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent findGorger(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Baloth Gorger"))
                .findFirst().orElse(null);
    }
}
