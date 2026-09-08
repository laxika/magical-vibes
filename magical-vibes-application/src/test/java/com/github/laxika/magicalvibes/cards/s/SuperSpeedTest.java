package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuperSpeed.class, GrizzlyBears.class, Mountain.class})
class SuperSpeedTest extends BaseCardTest {

    @Test
    @DisplayName("Super Speed boosts the enchanted creature and grants first strike on entry")
    void resolvesAuraEffects() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SuperSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Super Speed's first strike grant wears off at cleanup but its static effects remain")
    void firstStrikeWearsOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SuperSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Super Speed cannot enchant a land")
    void cannotEnchantLand() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new SuperSpeed()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent mountain = findPermanent(player1, "Mountain");
        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
