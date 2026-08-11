package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StillLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Before activation, Still Life is not a creature")
    void isNotCreatureBeforeActivation() {
        Permanent stillLife = addStillLife();

        assertThat(gqs.isCreature(gd, stillLife)).isFalse();
    }

    @Test
    @DisplayName("Activating Still Life makes it a 4/3 Centaur enchantment")
    void activationMakesItA4x3CentaurEnchantment() {
        Permanent stillLife = addStillLife();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, stillLife)).isTrue();
        assertThat(gqs.getEffectivePower(gd, stillLife)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stillLife)).isEqualTo(3);
        assertThat(stillLife.getTransientSubtypes()).containsExactly(CardSubtype.CENTAUR);
        assertThat(gqs.isEnchantment(gd, stillLife)).isTrue();
    }

    @Test
    @DisplayName("Still Life's animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent stillLife = addStillLife();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, stillLife)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, stillLife)).isFalse();
        assertThat(stillLife.getTransientSubtypes()).isEmpty();
    }

    private Permanent addStillLife() {
        return harness.addToBattlefieldAndReturn(player1, new StillLife());
    }
}
