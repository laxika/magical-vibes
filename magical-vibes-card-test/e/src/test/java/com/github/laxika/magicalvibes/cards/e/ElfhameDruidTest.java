package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AcademyDrake;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElfhameDruid.class, AcademyDrake.class, BalothGorger.class})
class ElfhameDruidTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one green mana")
    void firstAbilityAddsGreen() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(0);
    }

    @Test
    @DisplayName("Second ability adds two kicked-only green mana")
    void secondAbilityAddsKickedOnlyGreen() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(false);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kicked-only green can pay for a kicked creature spell")
    void kickedOnlyGreenPaysForKickedSpell() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(false);

        // Activate second ability: 2 kicked-only green
        harness.activateAbility(player1, 0, 1, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Academy Drake: {2}{U} + kicker {4} = 1 blue + 6 generic
        // Pool: 2 kicked-only green + 1 blue + 4 colorless = 7 effective mana
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.castKickedCreature(player1, 0);

        // Should resolve without error — kicked-only green used for generic costs
        harness.passBothPriorities();

        // Academy Drake resolves and enters the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(0);
    }

    @Test
    @DisplayName("Kicked-only green is not spent when casting a non-kicked spell")
    void kickedOnlyGreenNotUsedForNonKickedSpell() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(false);

        // Activate second ability: 2 kicked-only green
        harness.activateAbility(player1, 0, 1, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Cast another Elfhame Druid ({1}{G}) with regular mana only
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new ElfhameDruid()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // The second Elfhame Druid enters the battlefield using regular green mana
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        // Kicked-only green should be untouched
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(2);
    }

    @Test
    @DisplayName("Kicked-only green can pay the kicker cost portion")
    void kickedOnlyGreenPaysKickerCost() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(false);

        // Activate second ability: 2 kicked-only green
        harness.activateAbility(player1, 0, 1, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Academy Drake: {2}{U} + kicker {4} = 7 mana total
        // Give enough regular mana for the main cost, let kicked-only green pay part of kicker
        // Main cost: {2}{U} = 3 mana. Kicker: {4} = 4 mana. Total: 7
        // Pool: 1 blue + 4 colorless (regular) + 2 kicked-only green = 7 mana
        // Kicked-only green pays 2 of the generic costs
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new AcademyDrake()));

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        // Should resolve — kicked-only green helped cover total generic cost
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Restricted green pays the green symbols in a kicked spell's base cost")
    void restrictedGreenPaysColoredBaseCost() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setHand(player1, List.of(new BalothGorger()));

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Baloth Gorger");
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Restricted green cannot pay for an unkicked spell that has kicker")
    void cannotUseRestrictedManaWithoutKicking() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new AcademyDrake()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(2);
    }

    @Test
    @DisplayName("Restricted green cannot substitute for blue in a kicked spell")
    void restrictedManaRetainsItsColor() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player1, List.of(new AcademyDrake()));

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both mana abilities resolve immediately and require tapping")
    void manaAbilitiesTapAndDoNotUseStack(int abilityIndex) {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(false);

        harness.activateAbility(player1, 0, abilityIndex, null, null);

        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(abilityIndex == 0 ? 1 : 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen())
                .isEqualTo(abilityIndex == 1 ? 2 : 0);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1 - abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Summoning sickness prevents both tap abilities")
    void summoningSicknessPreventsManaAbilities(int abilityIndex) {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ElfhameDruid());
        druid.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(druid.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getKickedOnlyGreen()).isZero();
    }
}
