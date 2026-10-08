package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.m.MidnightMangler;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildRoads.class, MidnightMangler.class, BrightfieldGlider.class})
class WildRoadsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no Mounts or Vehicles")
    void entersTappedWithoutMountOrVehicle() {
        harness.setHand(player1, List.of(new WildRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findWildRoads(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Vehicle")
    void entersUntappedWithVehicle() {
        harness.addToBattlefield(player1, new MidnightMangler());
        harness.setHand(player1, List.of(new WildRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findWildRoads(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces one green mana")
    void tapsForGreenMana() {
        Permanent roads = addWildRoadsReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(roads.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability creates an enhanced Pilot that can crew a Vehicle")
    void createsEnhancedPilot() {
        addWildRoadsReady(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new MidnightMangler());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wild Roads");
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
        addWildRoadsReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Enters untapped when you control a tapped Mount")
    void entersUntappedWithTappedMount() {
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        mount.tap();
        harness.setHand(player1, List.of(new WildRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findWildRoads(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Vehicle does not let Wild Roads enter untapped")
    void entersTappedWithOnlyOpponentsVehicle() {
        harness.addToBattlefield(player2, new MidnightMangler());
        harness.setHand(player1, List.of(new WildRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findWildRoads(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("The newly created Pilot can pay saddle 3 without increasing its actual power")
    void pilotSaddlesMount() {
        addWildRoadsReady(player1);
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertNotOnBattlefield(player1, "Wild Roads");
        harness.assertNotOnBattlefield(player1, "Pilot");
        harness.passBothPriorities();
        Permanent pilot = findPermanent(player1, "Pilot");

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mount), null, null);
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A tapped Wild Roads cannot activate its sacrifice ability")
    void tappedLandCannotCreatePilot() {
        Permanent roads = addWildRoadsReady(player1);
        roads.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Wild Roads");
        harness.assertNotOnBattlefield(player1, "Pilot");
    }

    private Permanent addWildRoadsReady(Player player) {
        return addCreatureReady(player, new WildRoads());
    }

    private Permanent findWildRoads(Player player) {
        return findPermanent(player, "Wild Roads");
    }
}
