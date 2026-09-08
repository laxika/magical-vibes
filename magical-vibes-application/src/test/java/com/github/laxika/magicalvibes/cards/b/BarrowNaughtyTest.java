package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarrowNaughty.class, CloudSprite.class, GrizzlyBears.class})
class BarrowNaughtyTest extends BaseCardTest {

    @Test
    @DisplayName("Has lifelink only while you control another Faerie")
    void conditionalLifelink() {
        Permanent naughty = harness.addToBattlefieldAndReturn(player1, new BarrowNaughty());

        assertThat(gqs.hasKeyword(gd, naughty, Keyword.LIFELINK)).isFalse();

        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.hasKeyword(gd, naughty, Keyword.LIFELINK)).isFalse();

        harness.addToBattlefield(player2, new CloudSprite());
        assertThat(gqs.hasKeyword(gd, naughty, Keyword.LIFELINK)).isFalse();

        harness.addToBattlefield(player1, new CloudSprite());
        assertThat(gqs.hasKeyword(gd, naughty, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Activated ability gives +1/+0 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent naughty = harness.addToBattlefieldAndReturn(player1, new BarrowNaughty());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, naughty)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, naughty)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, naughty)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, naughty)).isEqualTo(3);
    }
}
