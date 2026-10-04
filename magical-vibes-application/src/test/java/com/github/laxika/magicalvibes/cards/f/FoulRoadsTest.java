package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.a.AutarchMammoth;
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

@CardUsed({FoulRoads.class, DuskLegionDreadnought.class, AutarchMammoth.class})
class FoulRoadsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no Mounts or Vehicles")
    void entersTappedWithoutMountOrVehicle() {
        harness.setHand(player1, List.of(new FoulRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findFoulRoads(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Vehicle")
    void entersUntappedWithVehicle() {
        harness.addToBattlefield(player1, new DuskLegionDreadnought());
        harness.setHand(player1, List.of(new FoulRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findFoulRoads(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces one black mana")
    void tapsForBlackMana() {
        Permanent roads = addFoulRoadsReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(roads.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability creates an enhanced Pilot that can crew a Vehicle")
    void createsEnhancedPilot() {
        addFoulRoadsReady(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Foul Roads");
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
        addFoulRoadsReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void entersUntappedWithMount() {
        harness.addToBattlefield(player1, new AutarchMammoth());
        harness.setHand(player1, List.of(new FoulRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findFoulRoads(player1).isTapped()).isFalse();
    }

    @Test
    void opponentsMountAndVehicleDoNotPreventEnteringTapped() {
        harness.addToBattlefield(player2, new AutarchMammoth());
        harness.addToBattlefield(player2, new DuskLegionDreadnought());
        harness.setHand(player1, List.of(new FoulRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findFoulRoads(player1).isTapped()).isTrue();
    }

    @Test
    void pilotsCountAsThreePowerEachForSaddleWithoutChangingActualPower() {
        addFoulRoadsReady(player1);
        addFoulRoadsReady(player1);
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new AutarchMammoth());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> pilots = findPermanents(player1, "Pilot");
        assertThat(pilots).hasSize(2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mount), 0, null, null);
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(pilots).allSatisfy(pilot -> {
            assertThat(pilot.isTapped()).isTrue();
            assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, pilot)).isEqualTo(1);
        });
    }

    @Test
    void sacrificeIsPaidBeforePilotResolves() {
        addFoulRoadsReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Foul Roads");
        harness.assertInGraveyard(player1, "Foul Roads");
        assertThat(countPermanents(player1, "Pilot")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Pilot")).isEqualTo(1);
        assertThat(countPermanents(player2, "Pilot")).isZero();
        assertThat(findPermanent(player1, "Pilot").isTapped()).isFalse();
    }

    @Test
    void tappedLandCannotPaySacrificeAbilityTapCost() {
        Permanent roads = addFoulRoadsReady(player1);
        roads.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertOnBattlefield(player1, "Foul Roads");
        assertThat(countPermanents(player1, "Pilot")).isZero();
    }

    @Test
    void sacrificeAbilityCannotBeActivatedDuringOpponentsMainPhase() {
        addFoulRoadsReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertOnBattlefield(player1, "Foul Roads");
    }

    @Test
    void sacrificeAbilityCannotBeActivatedWithNonemptyStack() {
        addFoulRoadsReady(player1);
        addFoulRoadsReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Foul Roads")).isEqualTo(1);
        assertThat(countPermanents(player1, "Pilot")).isEqualTo(1);
    }

    private Permanent addFoulRoadsReady(Player player) {
        return addCreatureReady(player, new FoulRoads());
    }

    private Permanent findFoulRoads(Player player) {
        return findPermanent(player, "Foul Roads");
    }
}
