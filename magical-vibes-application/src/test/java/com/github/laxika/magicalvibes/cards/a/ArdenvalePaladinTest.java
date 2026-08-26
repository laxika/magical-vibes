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

@CardUsed({ArdenvalePaladin.class})
class ArdenvalePaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when at least three white mana is spent")
    void entersWithCounterWhenThreeWhiteManaIsSpent() {
        harness.setHand(player1, List.of(new ArdenvalePaladin()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Ardenvale Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not enter with a counter when fewer than three white mana is spent")
    void doesNotEnterWithCounterWhenFewerThanThreeWhiteManaIsSpent() {
        harness.setHand(player1, List.of(new ArdenvalePaladin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent paladin = findPermanent(player1, "Ardenvale Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
