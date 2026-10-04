package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmberethPaladin.class})
class EmberethPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when at least three red mana is spent")
    void entersWithCounterWhenThreeRedManaIsSpent() {
        harness.castFromHand(player1, new EmberethPaladin(), "{1}{R}{R}{R}");
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Embereth Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not enter with a counter when fewer than three red mana is spent")
    void doesNotEnterWithCounterWhenFewerThanThreeRedManaIsSpent() {
        harness.castFromHand(player1, new EmberethPaladin(), "{2}{R}{R}");
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Embereth Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Four red mana gives exactly one counter as it enters without a trigger")
    void entersWithOneCounterWhenFourRedManaIsSpent() {
        harness.castFromHand(player1, new EmberethPaladin(), "{R}{R}{R}{R}");
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Embereth Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Red mana added after casting does not satisfy adamant")
    void redManaInPoolDoesNotCountAsManaSpent() {
        harness.castFromHand(player1, new EmberethPaladin(), "{3}{R}");
        harness.addMana(player1, ManaColor.RED, 3);
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Embereth Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Can attack on the turn it enters even without adamant")
    void canAttackOnTheTurnItEntersWithoutAdamant() {
        harness.castFromHand(player1, new EmberethPaladin(), "{3}{R}");
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        Permanent paladin = findPermanent(player1, "Embereth Paladin");
        assertThat(paladin.isTapped()).isTrue();
        assertThat(paladin.isAttacking()).isTrue();
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
