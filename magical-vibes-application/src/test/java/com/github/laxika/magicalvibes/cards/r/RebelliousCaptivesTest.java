package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RebelliousCaptives.class, Forest.class})
class RebelliousCaptivesTest extends BaseCardTest {

    @Test
    void exhaustPutsCountersOnItAndEarthbendsLand() {
        Permanent captives = harness.addToBattlefieldAndReturn(player1, new RebelliousCaptives());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(captives.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotEarthbendLandControlledByOpponent() {
        harness.addToBattlefield(player1, new RebelliousCaptives());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exhaustCanBeActivatedOnlyOnce() {
        harness.addToBattlefield(player1, new RebelliousCaptives());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
