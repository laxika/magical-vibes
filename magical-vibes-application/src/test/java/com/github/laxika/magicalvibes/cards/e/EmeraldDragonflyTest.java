package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed(EmeraldDragonfly.class)
class EmeraldDragonflyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants first strike until end of turn")
    void resolvingAbilityGrantsFirstStrike() {
        Permanent dragonfly = addCreatureReady(player1, new EmeraldDragonfly());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragonfly, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent dragonfly = addCreatureReady(player1, new EmeraldDragonfly());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, dragonfly, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Ability requires {G}{G}")
    void requiresTwoGreenMana() {
        addCreatureReady(player1, new EmeraldDragonfly());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability cannot use colorless mana for {G}")
    void cannotUseColorlessManaForGreenCost() {
        addCreatureReady(player1, new EmeraldDragonfly());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated while tapped")
    void canActivateWhileTapped() {
        Permanent dragonfly = addCreatureReady(player1, new EmeraldDragonfly());
        dragonfly.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(dragonfly.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability does not tap Emerald Dragonfly")
    void abilityDoesNotTapDragonfly() {
        Permanent dragonfly = addCreatureReady(player1, new EmeraldDragonfly());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(dragonfly.isTapped()).isFalse();
    }
}
