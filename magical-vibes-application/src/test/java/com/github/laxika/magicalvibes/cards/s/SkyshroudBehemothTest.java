package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkyshroudBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Skyshroud Behemoth enters tapped with two fade counters")
    void entersTappedWithFadeCounters() {
        harness.setHand(player1, List.of(new SkyshroudBehemoth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent behemoth = findPermanent(player1, "Skyshroud Behemoth");
        assertThat(behemoth.isTapped()).isTrue();
        assertThat(behemoth.getCounterCount(CounterType.FADE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fading removes one fade counter during Skyshroud Behemoth's controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent behemoth = addCreatureReady(player1, new SkyshroudBehemoth());
        behemoth.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(behemoth.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Skyshroud Behemoth");
    }

    @Test
    @DisplayName("Fading sacrifices Skyshroud Behemoth when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addCreatureReady(player1, new SkyshroudBehemoth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyshroud Behemoth");
    }
}
