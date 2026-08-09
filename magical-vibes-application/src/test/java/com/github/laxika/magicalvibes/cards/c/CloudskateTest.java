package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CloudskateTest extends BaseCardTest {

    @Test
    @DisplayName("Cloudskate enters with three fade counters")
    void entersWithFadeCounters() {
        harness.setHand(player1, List.of(new Cloudskate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent cloudskate = findPermanent(player1, "Cloudskate");
        assertThat(cloudskate.getCounterCount(CounterType.FADE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fading removes one fade counter during its controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent cloudskate = addCreatureReady(player1, new Cloudskate());
        cloudskate.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(cloudskate.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Cloudskate");
    }

    @Test
    @DisplayName("Fading sacrifices Cloudskate when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addCreatureReady(player1, new Cloudskate());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cloudskate");
    }
}
