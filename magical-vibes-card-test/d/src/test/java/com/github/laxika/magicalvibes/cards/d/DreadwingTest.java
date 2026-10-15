package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dreadwing.class})
class DreadwingTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives +3/+0 and flying until end of turn")
    void boostsAndGrantsFlying() {
        Permanent dreadwing = harness.addToBattlefieldAndReturn(player1, new Dreadwing());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dreadwing)).isEqualTo(4);   // 1 + 3
        assertThat(gqs.getEffectiveToughness(gd, dreadwing)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dreadwing, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent dreadwing = harness.addToBattlefieldAndReturn(player1, new Dreadwing());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dreadwing, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dreadwing)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dreadwing)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dreadwing, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Repeated activations stack only on their source and expire together")
    void repeatedActivationsOnlyBoostTheirSource() {
        Permanent dreadwing = harness.addToBattlefieldAndReturn(player1, new Dreadwing());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new Dreadwing());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Dreadwing());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.getEffectivePower(gd, dreadwing)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dreadwing, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dreadwing)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, dreadwing)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dreadwing, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dreadwing)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dreadwing)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dreadwing, Keyword.FLYING)).isFalse();
    }
}
