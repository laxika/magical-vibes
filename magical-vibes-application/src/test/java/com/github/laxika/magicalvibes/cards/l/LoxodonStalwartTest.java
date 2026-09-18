package com.github.laxika.magicalvibes.cards.l;

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

@CardUsed(LoxodonStalwart.class)
class LoxodonStalwartTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives Loxodon Stalwart +0/+1")
    void resolvingAbilityBoostsToughness() {
        addCreatureReady(player1, new LoxodonStalwart());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent stalwart = findPermanent(player1, "Loxodon Stalwart");
        assertThat(stalwart.getEffectivePower()).isEqualTo(3);
        assertThat(stalwart.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple activations accumulate")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new LoxodonStalwart());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent stalwart = findPermanent(player1, "Loxodon Stalwart");
        assertThat(stalwart.getEffectivePower()).isEqualTo(3);
        assertThat(stalwart.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The ability can be activated while Loxodon Stalwart is tapped")
    void canActivateWhileTapped() {
        Permanent stalwart = addCreatureReady(player1, new LoxodonStalwart());
        stalwart.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(stalwart.isTapped()).isTrue();
        assertThat(stalwart.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new LoxodonStalwart());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent stalwart = findPermanent(player1, "Loxodon Stalwart");
        assertThat(stalwart.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(stalwart.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability resolves without effect if Loxodon Stalwart leaves before resolution")
    void abilityResolvesWithoutEffectIfSourceRemoved() {
        Permanent stalwart = addCreatureReady(player1, new LoxodonStalwart());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(stalwart.getEffectiveToughness()).isEqualTo(3);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without white mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new LoxodonStalwart());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability cannot be activated with only non-white mana")
    void cannotActivateWithOnlyNonWhiteMana() {
        addCreatureReady(player1, new LoxodonStalwart());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Vigilance keeps Loxodon Stalwart untapped when attacking")
    void vigilanceKeepsItUntappedWhenAttacking() {
        Permanent stalwart = addCreatureReady(player1, new LoxodonStalwart());

        declareAttackers(List.of(0));

        assertThat(stalwart.isTapped()).isFalse();
    }
}
