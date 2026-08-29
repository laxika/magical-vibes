package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DarkDepthsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with ten ice counters")
    void entersWithTenIceCounters() {
        harness.setHand(player1, List.of(new DarkDepths()));

        harness.playLand(player1, 0);

        Permanent darkDepths = findPermanent(player1, "Dark Depths");
        assertThat(darkDepths.getCounterCount(CounterType.ICE)).isEqualTo(10);
    }

    @Test
    @DisplayName("Removing the last ice counter sacrifices Dark Depths and creates Marit Lage")
    void removesLastIceCounterAndCreatesMaritLage() {
        Permanent darkDepths = addDarkDepths();
        darkDepths.setCounterCount(CounterType.ICE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(darkDepths.getCounterCount(CounterType.ICE)).isZero();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dark Depths");
        Permanent maritLage = findPermanent(player1, "Marit Lage");
        assertThat(maritLage.getEffectivePower()).isEqualTo(20);
        assertThat(maritLage.getEffectiveToughness()).isEqualTo(20);
        assertThat(maritLage.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(maritLage.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot remove an ice counter when none remain")
    void cannotRemoveIceCounterWhenNoneRemain() {
        Permanent darkDepths = addDarkDepths();
        darkDepths.setCounterCount(CounterType.ICE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addDarkDepths() {
        Permanent darkDepths = harness.addToBattlefieldAndReturn(player1, new DarkDepths());
        darkDepths.setSummoningSick(false);
        return darkDepths;
    }
}
