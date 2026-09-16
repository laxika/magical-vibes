package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(NantukoShade.class)
class NantukoShadeTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Nantuko Shade puts it on the stack")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new NantukoShade(), "{B}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving Nantuko Shade puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.castFromHand(player1, new NantukoShade(), "{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent shadePerm = addCreatureReady(player1, new NantukoShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(shadePerm.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+1 to Nantuko Shade")
    void resolvingAbilityBoostsPowerAndToughness() {
        addCreatureReady(player1, new NantukoShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent shade = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(3);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        addCreatureReady(player1, new NantukoShade());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(5);
        assertThat(shade.getEffectiveToughness()).isEqualTo(4);
        assertThat(shade.getPowerModifier()).isEqualTo(3);
        assertThat(shade.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addCreatureReady(player1, new NantukoShade());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(4);
        assertThat(shade.getEffectiveToughness()).isEqualTo(3);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from END to CLEANUP

        assertThat(shade.getPowerModifier()).isEqualTo(0);
        assertThat(shade.getToughnessModifier()).isEqualTo(0);
        assertThat(shade.getEffectivePower()).isEqualTo(2);
        assertThat(shade.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new NantukoShade());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate ability with summoning sickness because it does not tap")
    void canActivateWithSummoningSickness() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new NantukoShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(3);
        assertThat(shade.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability with only colorless mana")
    void cannotActivateWithOnlyColorlessMana() {
        addCreatureReady(player1, new NantukoShade());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
