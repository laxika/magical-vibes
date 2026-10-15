package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.a.AlacrianJaguar;
import com.github.laxika.magicalvibes.cards.t.ThunderingBroodwagon;
import com.github.laxika.magicalvibes.cards.v.VenomsacLagac;
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

@CardUsed({CountryRoads.class, DuskLegionDreadnought.class, AlacrianJaguar.class,
        ThunderingBroodwagon.class, VenomsacLagac.class})
class CountryRoadsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no Mounts or Vehicles")
    void entersTappedWithoutMountOrVehicle() {
        harness.setHand(player1, List.of(new CountryRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findCountryRoads(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Vehicle")
    void entersUntappedWithVehicle() {
        harness.addToBattlefield(player1, new DuskLegionDreadnought());
        harness.setHand(player1, List.of(new CountryRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findCountryRoads(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces one white mana")
    void tapsForWhiteMana() {
        Permanent roads = addCountryRoadsReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(roads.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability creates an enhanced Pilot that can crew a Vehicle")
    void createsEnhancedPilot() {
        addCountryRoadsReady(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Country Roads");
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
        addCountryRoadsReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void entersUntappedWithTappedMount() {
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new AlacrianJaguar());
        mount.tap();
        harness.setHand(player1, List.of(new CountryRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findCountryRoads(player1).isTapped()).isFalse();
    }

    @Test
    void opposingVehicleDoesNotAllowUntappedEntry() {
        harness.addToBattlefield(player2, new ThunderingBroodwagon());
        harness.setHand(player1, List.of(new CountryRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findCountryRoads(player1).isTapped()).isTrue();
    }

    @Test
    void sacrificeIsPaidBeforePilotResolvesAndPilotCanCrewThreeImmediately() {
        addCountryRoadsReady(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ThunderingBroodwagon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Country Roads");
        harness.assertInGraveyard(player1, "Country Roads");
        harness.assertNotOnBattlefield(player1, "Pilot");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();
        Permanent pilot = findPermanent(player1, "Pilot");
        assertThat(pilot.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pilot)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
    }

    @Test
    void pilotCanSaddleImmediatelyAfterCreation() {
        addCountryRoadsReady(player1);
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new VenomsacLagac());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(findPermanent(player1, "Pilot").isTapped()).isTrue();
    }

    @Test
    void tappedRoadsCannotPaySacrificeAbility() {
        Permanent roads = addCountryRoadsReady(player1);
        roads.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Country Roads");
        harness.assertNotOnBattlefield(player1, "Pilot");
    }

    private Permanent addCountryRoadsReady(Player player) {
        Permanent roads = harness.addToBattlefieldAndReturn(player, new CountryRoads());
        roads.setSummoningSick(false);
        return roads;
    }

    private Permanent findCountryRoads(Player player) {
        return findPermanent(player, "Country Roads");
    }
}
