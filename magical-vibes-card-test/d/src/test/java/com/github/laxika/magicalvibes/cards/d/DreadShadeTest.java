package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DreadShade.class})
class DreadShadeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dread Shade puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new DreadShade()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Dread Shade");
    }

    @Test
    @DisplayName("Resolving Dread Shade puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new DreadShade()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dread Shade");
    }

    @Test
    @DisplayName("Activating ability puts BoostSelf on the stack with self as target")
    void activatingAbilityPutsOnStack() {
        Permanent shadePerm = addDreadShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Dread Shade");
        assertThat(entry.getTargetId()).isEqualTo(shadePerm.getId());
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        addDreadShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving ability gives +1/+1 to Dread Shade")
    void resolvingAbilityBoostsPowerAndToughness() {
        addDreadShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent shade = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(4);
        assertThat(shade.getEffectiveToughness()).isEqualTo(4);
        assertThat(shade.getPowerModifier()).isEqualTo(1);
        assertThat(shade.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        addDreadShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(6);
        assertThat(shade.getEffectiveToughness()).isEqualTo(6);
        assertThat(shade.getPowerModifier()).isEqualTo(3);
        assertThat(shade.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addDreadShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent shade = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(shade.getEffectivePower()).isEqualTo(5);
        assertThat(shade.getEffectiveToughness()).isEqualTo(5);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from END to CLEANUP

        assertThat(shade.getPowerModifier()).isEqualTo(0);
        assertThat(shade.getToughnessModifier()).isEqualTo(0);
        assertThat(shade.getEffectivePower()).isEqualTo(3);
        assertThat(shade.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability resolves without effect if Dread Shade is removed before resolution")
    void abilityHasNoEffectIfSourceRemoved() {
        addDreadShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove Dread Shade before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addDreadShadeReady(player1);
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can activate while summoning sick and tapped")
    void canActivateWhileSummoningSickAndTapped() {
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new DreadShade());
        shade.setSummoningSick(true);
        shade.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(4);
        assertThat(shade.getEffectiveToughness()).isEqualTo(4);
        assertThat(shade.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple pending activations boost only their source")
    void pendingActivationsBoostOnlyTheirSource() {
        Permanent shade = addDreadShadeReady(player1);
        Permanent otherShade = harness.addToBattlefieldAndReturn(player1, new DreadShade());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(shade.getPowerModifier()).isZero();
        assertThat(harness.getGameData().stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(shade.getPowerModifier()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(shade.getEffectivePower()).isEqualTo(5);
        assertThat(shade.getEffectiveToughness()).isEqualTo(5);
        assertThat(otherShade.getPowerModifier()).isZero();
        assertThat(otherShade.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A pending ability does not boost the source after it leaves and reenters")
    void pendingAbilityDoesNotBoostNewPermanent() {
        Permanent original = addDreadShadeReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.getGameData().playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, original.getCard());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(returned.getPowerModifier()).isZero();
        assertThat(returned.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Nonblack mana cannot pay the activation cost")
    void cannotActivateWithNonblackMana() {
        addDreadShadeReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    private Permanent addDreadShadeReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DreadShade());
        perm.setSummoningSick(false);
        return perm;
    }
}
