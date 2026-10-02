package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DwarvenPatrol;
import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodfireDwarf.class, DwarvenPatrol.class, GaeasSkyfolk.class, YavimayaCoast.class})
class BloodfireDwarfTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it deals 1 damage to each creature without flying")
    void sacrificesItAndDamagesOnlyCreaturesWithoutFlying() {
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new BloodfireDwarf());
        Permanent ownGroundCreature = harness.addToBattlefieldAndReturn(player1, new DwarvenPatrol());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new DwarvenPatrol());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new GaeasSkyfolk());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaCoast());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bloodfire Dwarf");
        assertThat(ownGroundCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        assertThat(land.getMarkedDamage()).isZero();
        assertThat(dwarf).isNotIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("The ability requires one red mana")
    void requiresRedMana() {
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new BloodfireDwarf());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(dwarf);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick dwarf can activate and is sacrificed before damage resolves")
    void sacrificesImmediatelyWithoutTapOrSummoningRestrictions() {
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new BloodfireDwarf());
        dwarf.setTapped(true);
        dwarf.setSummoningSick(true);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DwarvenPatrol());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Bloodfire Dwarf");
        harness.assertNotOnBattlefield(player1, "Bloodfire Dwarf");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Damage kills creatures on both sides and includes creatures entering before resolution")
    void damagesCreaturesPresentAtResolution() {
        harness.addToBattlefield(player1, new BloodfireDwarf());
        harness.addToBattlefield(player1, new BloodfireDwarf());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player2, new BloodfireDwarf());
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new GaeasSkyfolk());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bloodfire Dwarf");
        harness.assertNotOnBattlefield(player2, "Bloodfire Dwarf");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof BloodfireDwarf).hasSize(2);
        harness.assertInGraveyard(player2, "Bloodfire Dwarf");
        harness.assertOnBattlefield(player2, "Gaea's Skyfolk");
        assertThat(flyer.getMarkedDamage()).isZero();
    }
}
