package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlackbladeReforgedTest extends BaseCardTest {

    // ===== Boost per land =====

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each land controller controls")
    void boostsPerLand() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent blade = new Permanent(new BlackbladeReforged());
        blade.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(blade);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());

        // 2/2 base + 3 lands * +1/+1 = 5/5
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost counts all land types, not just specific subtypes")
    void countsAllLandTypes() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent blade = new Permanent(new BlackbladeReforged());
        blade.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(blade);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());

        // 2/2 base + 4 lands * +1/+1 = 6/6
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost updates dynamically when land count changes")
    void updatesDynamicallyWithLandCount() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent blade = new Permanent(new BlackbladeReforged());
        blade.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(blade);

        // No lands — bears is 2/2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        // Remove all lands
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest")
                || p.getCard().getName().equals("Swamp"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts equipment controller's lands, not equipped creature's controller's")
    void countsEquipmentControllersLands() {
        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        // Blade controlled by player1, attached to player2's creature
        Permanent blade = new Permanent(new BlackbladeReforged());
        blade.setAttachedTo(opponentBears.getId());
        gd.playerBattlefields.get(player1.getId()).add(blade);

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        // Should count player1's 2 lands, not player2's 3
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(4); // 2 base + 2 lands
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count opponent's lands")
    void doesNotCountOpponentLands() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent blade = new Permanent(new BlackbladeReforged());
        blade.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(blade);

        // Only opponent has lands
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        // No boost from opponent's lands
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Equip legendary creature {3} =====

    @Test
    @DisplayName("Can equip legendary creature for {3}")
    void equipLegendaryCreatureForThree() {
        harness.addToBattlefield(player1, new Forest());

        Permanent arvad = new Permanent(new ArvadTheCursed());
        arvad.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(arvad);

        Permanent blade = new Permanent(new BlackbladeReforged());
        gd.playerBattlefields.get(player1.getId()).add(blade);

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
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent blade = new Permanent(new BlackbladeReforged());
        gd.playerBattlefields.get(player1.getId()).add(blade);

        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int bladeIndex = battlefield.indexOf(blade);

        assertThatThrownBy(() -> harness.activateAbility(player1, bladeIndex, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a legendary creature you control");
    }

    // ===== Equip {7} =====

    @Test
    @DisplayName("Can equip any creature for {7}")
    void equipAnyCreatureForSeven() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent blade = new Permanent(new BlackbladeReforged());
        gd.playerBattlefields.get(player1.getId()).add(blade);

        harness.addMana(player1, ManaColor.COLORLESS, 7);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int bladeIndex = battlefield.indexOf(blade);

        harness.activateAbility(player1, bladeIndex, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Equipping to another creature transfers the boost")
    void equipTransfersBoost() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Swamp());

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent arvad = new Permanent(new ArvadTheCursed());
        arvad.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(arvad);

        Permanent blade = new Permanent(new BlackbladeReforged());
        blade.setAttachedTo(arvad.getId());
        gd.playerBattlefields.get(player1.getId()).add(blade);

        // Arvad is 3/3 base + 3 lands = 6/6 (plus Arvad's own +2/+2 legendary lord to self is not applicable since it says "other")
        assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(6);

        // Equip to bears using {7} equip
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        int bladeIndex = battlefield.indexOf(blade);

        harness.activateAbility(player1, bladeIndex, 1, null, bears.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(bears.getId());

        // Bears gets 2/2 base + 3 lands = 5/5 (plus Arvad's +2/+2 doesn't apply since Bears is not legendary)
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);

        // Arvad is back to 3/3 base (no equipment boost)
        assertThat(gqs.getEffectivePower(gd, arvad)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, arvad)).isEqualTo(3);
    }
}
