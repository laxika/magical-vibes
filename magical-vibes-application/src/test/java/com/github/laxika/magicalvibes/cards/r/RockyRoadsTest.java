package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RockyRoadsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no Mounts or Vehicles")
    void entersTappedWithoutMountOrVehicle() {
        harness.setHand(player1, List.of(new RockyRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findRockyRoads(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Vehicle")
    void entersUntappedWithVehicle() {
        harness.addToBattlefield(player1, new DuskLegionDreadnought());
        harness.setHand(player1, List.of(new RockyRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findRockyRoads(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces one red mana")
    void tapsForRedMana() {
        Permanent roads = addRockyRoadsReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(roads.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability creates an enhanced Pilot that can crew a Vehicle")
    void createsEnhancedPilot() {
        addRockyRoadsReady(player1);
        Permanent vehicle = new Permanent(new DuskLegionDreadnought());
        gd.playerBattlefields.get(player1.getId()).add(vehicle);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rocky Roads");
        Permanent pilot = findPermanent(player1, "Pilot");
        assertThat(pilot.getCard().getSubtypes()).contains(CardSubtype.PILOT);
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability can only be activated as a sorcery")
    void sacrificeAbilityRequiresSorcerySpeed() {
        addRockyRoadsReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addRockyRoadsReady(Player player) {
        Permanent roads = new Permanent(new RockyRoads());
        roads.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(roads);
        return roads;
    }

    private Permanent findRockyRoads(Player player) {
        return findPermanent(player, "Rocky Roads");
    }
}
