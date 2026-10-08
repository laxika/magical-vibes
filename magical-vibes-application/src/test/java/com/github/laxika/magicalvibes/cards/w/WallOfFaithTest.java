package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({WallOfFaith.class})
class WallOfFaithTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Wall of Faith puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new WallOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wall of Faith");
    }

    @Test
    @DisplayName("Resolving Wall of Faith puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new WallOfFaith()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wall of Faith");
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack without immediately boosting the source")
    void activatingAbilityPutsOnStack() {
        Permanent wallPerm = addWallOfFaithReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Wall of Faith");
        assertThat(wallPerm.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        addWallOfFaithReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        Permanent wall = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving ability gives +0/+1 to Wall of Faith")
    void resolvingAbilityBoostsToughness() {
        addWallOfFaithReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent wall = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wall.getEffectivePower()).isEqualTo(0);
        assertThat(wall.getEffectiveToughness()).isEqualTo(6);
        assertThat(wall.getToughnessModifier()).isEqualTo(1);
        assertThat(wall.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can activate ability multiple times if mana allows")
    void canActivateMultipleTimes() {
        addWallOfFaithReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wall = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wall.getEffectivePower()).isEqualTo(0);
        assertThat(wall.getEffectiveToughness()).isEqualTo(8);
        assertThat(wall.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addWallOfFaithReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        addWallOfFaithReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wall = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(wall.getEffectiveToughness()).isEqualTo(7);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wall.getToughnessModifier()).isEqualTo(0);
        assertThat(wall.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Ability resolves without a boost if Wall of Faith has left the battlefield")
    void abilityDoesNothingIfSourceRemoved() {
        addWallOfFaithReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        // Remove Wall of Faith before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addWallOfFaithReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfFaith());
        wall.setSummoningSick(true);
        wall.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.getEffectiveToughness()).isEqualTo(6);
        assertThat(wall.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot be paid for with blue mana")
    void cannotActivateWithWrongColorMana() {
        addWallOfFaithReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("An activation boosts only its source, not other Walls of Faith")
    void boostsOnlyItsSource() {
        Permanent source = addWallOfFaithReady(player1);
        Permanent other = addWallOfFaithReady(player1);
        Permanent opposing = addWallOfFaithReady(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(source.getEffectiveToughness()).isEqualTo(6);
        assertThat(other.getEffectiveToughness()).isEqualTo(5);
        assertThat(opposing.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("An activation does not boost its source after it leaves and returns")
    void doesNotBoostReturnedSource() {
        Permanent original = addWallOfFaithReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent returned = harness.addToBattlefieldAndReturn(player1, original.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(returned.getEffectiveToughness()).isEqualTo(5);
    }

    private Permanent addWallOfFaithReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new WallOfFaith());
        perm.setSummoningSick(false);
        return perm;
    }
}
