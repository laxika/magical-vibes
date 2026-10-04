package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieryHellhound.class})
class FieryHellhoundTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Fiery Hellhound puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new FieryHellhound()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(FieryHellhound.class);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack with its source recorded")
    void activatingAbilityPutsOnStack() {
        Permanent hellhound = addReadyFieryHellhound(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(FieryHellhound.class);
        assertThat(entry.getTargetId()).isEqualTo(hellhound.getId());
    }

    @Test
    @DisplayName("Resolving ability gives +1/+0 to Fiery Hellhound")
    void resolvingAbilityBoostsPower() {
        Permanent hellhound = addReadyFieryHellhound(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellhound.getEffectivePower()).isEqualTo(3);
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(2);
        assertThat(hellhound.getPowerModifier()).isEqualTo(1);
        assertThat(hellhound.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        Permanent hellhound = addReadyFieryHellhound(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellhound.getEffectivePower()).isEqualTo(5);
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(2);
        assertThat(hellhound.getPowerModifier()).isEqualTo(3);
        assertThat(hellhound.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent hellhound = addReadyFieryHellhound(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellhound.getEffectivePower()).isEqualTo(4);
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hellhound.getPowerModifier()).isEqualTo(0);
        assertThat(hellhound.getToughnessModifier()).isEqualTo(0);
        assertThat(hellhound.getEffectivePower()).isEqualTo(2);
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyFieryHellhound(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyFieryHellhound(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new FieryHellhound());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent hellhound = harness.addToBattlefieldAndReturn(player1, new FieryHellhound());
        hellhound.setSummoningSick(true);
        hellhound.setTapped(true);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hellhound.getEffectivePower()).isEqualTo(3);
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(2);
        assertThat(hellhound.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pending activations resolve separately and boost only their source")
    void pendingActivationsBoostOnlyTheirSource() {
        Permanent hellhound = addReadyFieryHellhound(player1);
        Permanent other = addReadyFieryHellhound(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(hellhound.getEffectivePower()).isEqualTo(2);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(hellhound.getEffectivePower()).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(hellhound.getEffectivePower()).isEqualTo(4);
        assertThat(hellhound.getEffectiveToughness()).isEqualTo(2);
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability cannot be paid for with nonred mana")
    void cannotActivateWithNonredMana() {
        Permanent hellhound = addReadyFieryHellhound(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(hellhound.getEffectivePower()).isEqualTo(2);
    }
}
