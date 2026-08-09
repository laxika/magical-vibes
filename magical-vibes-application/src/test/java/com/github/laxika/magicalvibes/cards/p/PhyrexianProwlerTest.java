package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Phyrexian Prowler enters with three fade counters")
    void entersWithFadeCounters() {
        harness.setHand(player1, List.of(new PhyrexianProwler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent prowler = findPermanent(player1, "Phyrexian Prowler");
        assertThat(prowler.getCounterCount(CounterType.FADE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fading removes one fade counter during its controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent prowler = addCreatureReady(player1, new PhyrexianProwler());
        prowler.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(prowler.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Phyrexian Prowler");
    }

    @Test
    @DisplayName("Fading sacrifices Phyrexian Prowler when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addCreatureReady(player1, new PhyrexianProwler());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Prowler");
    }

    @Test
    @DisplayName("Removing a fade counter gives Phyrexian Prowler +1/+1 until end of turn")
    void removesCounterAndBoostsSelf() {
        Permanent prowler = addCreatureReady(player1, new PhyrexianProwler());
        prowler.setCounterCount(CounterType.FADE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(prowler.getCounterCount(CounterType.FADE)).isZero();
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(4);
    }
}
