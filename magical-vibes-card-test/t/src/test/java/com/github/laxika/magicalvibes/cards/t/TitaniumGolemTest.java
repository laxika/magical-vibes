package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(TitaniumGolem.class)
class TitaniumGolemTest extends BaseCardTest {

    @Test
    @DisplayName("One white and one colorless mana pay the activation cost")
    void acceptsMixedManaPayment() {
        Permanent golem = addCreatureReady(player1, new TitaniumGolem());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(golem.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped summoning-sick Golem can activate its ability")
    void activatesWhileTappedAndSummoningSick() {
        Permanent golem = addCreatureReady(player1, new TitaniumGolem());
        golem.setSummoningSick(true);
        golem.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(golem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability grants first strike only to its source")
    void grantsFirstStrikeOnlyToSource() {
        Permanent source = addCreatureReady(player1, new TitaniumGolem());
        Permanent other = addCreatureReady(player1, new TitaniumGolem());
        Permanent opponent = addCreatureReady(player2, new TitaniumGolem());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Resolving the ability grants first strike until end of turn")
    void resolvingAbilityGrantsFirstStrike() {
        Permanent golem = addCreatureReady(player1, new TitaniumGolem());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent golem = addCreatureReady(player1, new TitaniumGolem());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Ability requires both generic and white mana")
    void requiresBothGenericAndWhiteMana() {
        addCreatureReady(player1, new TitaniumGolem());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability requires white mana even when generic mana is available")
    void requiresWhiteMana() {
        addCreatureReady(player1, new TitaniumGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
