package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.c.CabalEvangel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({BlackbladeReforged.class, ArvadTheCursed.class, CabalEvangel.class,
        Forest.class, Island.class, Plains.class, Swamp.class})
class BlackbladeReforgedTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each land controller controls")
    void boostsPerLand() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new CabalEvangel());

        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        blade.setAttachedTo(evangel.getId());

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());

        // 2/2 base + 3 lands * +1/+1 = 5/5
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost counts all land types, not just specific subtypes")
    void countsAllLandTypes() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new CabalEvangel());

        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        blade.setAttachedTo(evangel.getId());

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());

        // 2/2 base + 4 lands * +1/+1 = 6/6
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost updates dynamically when land count changes")
    void updatesDynamicallyWithLandCount() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new CabalEvangel());

        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        blade.setAttachedTo(evangel.getId());

        // No lands — evangel is 2/2
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(3);

        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(4);

        // Remove all lands
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest")
                || p.getCard().getName().equals("Swamp"));
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts equipment controller's lands, not equipped creature's controller's")
    void countsEquipmentControllersLands() {
        Permanent opponentEvangel = harness.addToBattlefieldAndReturn(player2, new CabalEvangel());

        // Blade controlled by player1, attached to player2's creature
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        blade.setAttachedTo(opponentEvangel.getId());

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        // Should count player1's 2 lands, not player2's 3
        assertThat(gqs.getEffectivePower(gd, opponentEvangel)).isEqualTo(4); // 2 base + 2 lands
        assertThat(gqs.getEffectiveToughness(gd, opponentEvangel)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count opponent's lands")
    void doesNotCountOpponentLands() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new CabalEvangel());

        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        blade.setAttachedTo(evangel.getId());

        // Only opponent has lands
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        // No boost from opponent's lands
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can equip legendary creature for {3}")
    void equipLegendaryCreatureForThree() {
        harness.addToBattlefield(player1, new Forest());

        Permanent arvad = addCreatureReady(player1, new ArvadTheCursed());

        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int bladeIndex = battlefield.indexOf(blade);

        harness.activateAbility(player1, bladeIndex, 0, null, arvad.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(arvad.getId());
    }

    @Test
    @DisplayName("Cannot equip non-legendary creature with the {3} equip ability")
    void cannotEquipNonLegendaryForThree() {
        Permanent evangel = addCreatureReady(player1, new CabalEvangel());

        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int bladeIndex = battlefield.indexOf(blade);

        assertThatThrownBy(() -> harness.activateAbility(player1, bladeIndex, 0, null, evangel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a legendary creature you control");
    }

    @Test
    @DisplayName("Can equip any creature for {7}")
    void equipAnyCreatureForSeven() {
        Permanent evangel = addCreatureReady(player1, new CabalEvangel());

        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());

        harness.addMana(player1, ManaColor.COLORLESS, 7);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int bladeIndex = battlefield.indexOf(blade);

        harness.activateAbility(player1, bladeIndex, 1, null, evangel.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(evangel.getId());
    }

    @Test
    @DisplayName("Equipping to another creature transfers the boost")
    void equipTransfersBoost() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Swamp());

        Permanent evangel = addCreatureReady(player1, new CabalEvangel());

        Permanent arvad = addCreatureReady(player1, new ArvadTheCursed());

        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        blade.setAttachedTo(arvad.getId());

        // Arvad is 3/3 base + 3 lands = 6/6 (plus Arvad's own +2/+2 legendary lord to self is not applicable since it says "other")
        assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(6);

        // Equip to evangel using {7} equip
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int bladeIndex = battlefield.indexOf(blade);

        harness.activateAbility(player1, bladeIndex, 1, null, evangel.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(evangel.getId());

        // Cabal Evangel gets 2/2 base + 3 lands = 5/5 (plus Arvad's +2/+2 doesn't apply since Cabal Evangel is not legendary)
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(5);

        // Arvad is back to 3/3 base (no equipment boost)
        assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(3);
    }

    @Test
    @DisplayName("Both equip abilities reject an opponent's legendary creature")
    void cannotEquipOpponentsCreature() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player2, new ArvadTheCursed());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 0, index, null, arvad.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Both equip abilities can only be activated during a main phase")
    void cannotEquipDuringUpkeep() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.forceStep(TurnStep.UPKEEP);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 1, index, null, arvad.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sorcery speed");
        }
        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Legendary equip requires all three mana")
    void legendaryEquipRequiresThreeMana() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, arvad.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ordinary equip requires seven mana even for a legendary creature")
    void ordinaryEquipRequiresSevenMana() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, arvad.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip leaves the current attachment intact when its target leaves")
    void targetLeavingDoesNotDetachEquipment() {
        Permanent evangel = harness.addToBattlefieldAndReturn(player1, new CabalEvangel());
        Permanent arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        blade.setAttachedTo(evangel.getId());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 2, 0, null, arvad.getId());
        gd.playerBattlefields.get(player1.getId()).remove(arvad);
        gd.playerGraveyards.get(player1.getId()).add(arvad.getCard());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(evangel.getId());
        assertThat(gqs.getEffectivePower(gd, evangel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, evangel)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Neither equip ability can target a land")
    void cannotEquipLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 1, index, null, land.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Neither equip ability can be activated with another ability on the stack")
    void cannotEquipWithNonemptyStack() {
        Permanent arvad = harness.addToBattlefieldAndReturn(player1, new ArvadTheCursed());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new BlackbladeReforged());
        harness.addMana(player1, ManaColor.COLORLESS, 20);
        harness.activateAbility(player1, 1, 0, null, arvad.getId());

        for (int abilityIndex = 0; abilityIndex < 2; abilityIndex++) {
            int index = abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, 1, index, null, arvad.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
        assertThat(gd.stack).hasSize(1);
        assertThat(blade.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(blade.getAttachedTo()).isEqualTo(arvad.getId());
    }
}
