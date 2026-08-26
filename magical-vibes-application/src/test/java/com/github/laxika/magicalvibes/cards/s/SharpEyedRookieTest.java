package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BorderlandMarauder;
import com.github.laxika.magicalvibes.cards.g.GiantTortoise;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SharpEyedRookie.class, BorderlandMarauder.class, GiantTortoise.class, GrizzlyBears.class})
class SharpEyedRookieTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on itself and investigates when an entering creature has greater power")
    void triggersForGreaterPower() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());

        harness.setHand(player1, List.of(new BorderlandMarauder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Triggers when an entering creature has greater toughness")
    void triggersForGreaterToughness() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());

        harness.setHand(player1, List.of(new GiantTortoise()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when an entering creature has neither greater power nor toughness")
    void doesNotTriggerWhenNeitherCharacteristicIsGreater() {
        Permanent rookie = harness.addToBattlefieldAndReturn(player1, new SharpEyedRookie());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(rookie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }
}
