package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(StillLife.class)
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
    @DisplayName("Activating Still Life requires two green mana")
    void activationRequiresTwoGreenMana() {
        addStillLife();
        harness.addMana(player1, ManaColor.GREEN, 1);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> harness.activateAbility(player1, 0, null, null)
        );
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
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, stillLife)).isFalse();
        assertThat(stillLife.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("Still Life does not animate until its ability resolves")
    void animationWaitsForResolution() {
        Permanent stillLife = addStillLife();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.isCreature(gd, stillLife)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, stillLife)).isTrue();
        assertThat(stillLife.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Still Life can animate while tapped and summoning sick on an opponent's turn")
    void canAnimateWhileTappedOnOpponentsTurn() {
        Permanent stillLife = addStillLife();
        stillLife.tap();
        stillLife.setSummoningSick(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, stillLife)).isTrue();
        assertThat(gqs.getEffectivePower(gd, stillLife)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stillLife)).isEqualTo(3);
        assertThat(stillLife.isTapped()).isTrue();
        assertThat(stillLife.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Repeated activations animate only their source and do not add power or toughness")
    void repeatedActivationsOnlyAnimateTheirSource() {
        Permanent stillLife = addStillLife();
        Permanent otherStillLife = addStillLife();
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stillLife)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stillLife)).isEqualTo(3);
        assertThat(gqs.isEnchantment(gd, stillLife)).isTrue();
        assertThat(stillLife.getTransientSubtypes()).containsExactly(CardSubtype.CENTAUR);
        assertThat(gqs.isCreature(gd, otherStillLife)).isFalse();
    }

    private Permanent addStillLife() {
        return harness.addToBattlefieldAndReturn(player1, new StillLife());
    }
}
