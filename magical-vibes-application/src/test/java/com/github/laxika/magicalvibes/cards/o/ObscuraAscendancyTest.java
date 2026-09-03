package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ObscuraAscendancy.class, GrizzlyBears.class, Opt.class})
class ObscuraAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a soul counter and creates a Spirit for a spell with the matching mana value")
    void createsSpiritAtMatchingManaValue() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new ObscuraAscendancy());

        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ascendancy.getCounterCount(CounterType.SOUL)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Only one of two simultaneous matching spells creates a Spirit")
    void interveningIfRechecksSoulCounters() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new ObscuraAscendancy());

        harness.setHand(player1, List.of(new Opt(), new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ascendancy.getCounterCount(CounterType.SOUL)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("A spell with mana value two matches after one soul counter")
    void advancesToNextManaValue() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new ObscuraAscendancy());
        ascendancy.setCounterCount(CounterType.SOUL, 1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(ascendancy.getCounterCount(CounterType.SOUL)).isEqualTo(2);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Spirits get plus three plus three at five soul counters")
    void boostsSpiritsAtThreshold() {
        Permanent ascendancy = harness.addToBattlefieldAndReturn(player1, new ObscuraAscendancy());

        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent spirit = findPermanents(player1, "Spirit").getFirst();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);

        ascendancy.setCounterCount(CounterType.SOUL, 5);

        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
