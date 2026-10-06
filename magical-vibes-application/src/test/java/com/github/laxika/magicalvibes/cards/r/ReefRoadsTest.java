package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GloryheathLynx;
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

@CardUsed({ReefRoads.class, DuskLegionDreadnought.class, GloryheathLynx.class})
class ReefRoadsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no Mounts or Vehicles")
    void entersTappedWithoutMountOrVehicle() {
        harness.setHand(player1, List.of(new ReefRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findReefRoads(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Vehicle")
    void entersUntappedWithVehicle() {
        harness.addToBattlefield(player1, new DuskLegionDreadnought());
        harness.setHand(player1, List.of(new ReefRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(findReefRoads(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces one blue mana")
    void tapsForBlueMana() {
        Permanent roads = addReefRoadsReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(roads.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrifice ability creates an enhanced Pilot that can crew a Vehicle")
    void createsEnhancedPilot() {
        addReefRoadsReady(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Reef Roads");
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
        addReefRoadsReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
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
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new GloryheathLynx());
        mount.tap();
        harness.setHand(player1, List.of(new ReefRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findReefRoads(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's Mount and Vehicle do not allow untapped entry")
    void entersTappedWithOnlyOpposingMountAndVehicle() {
        harness.addToBattlefield(player2, new GloryheathLynx());
        harness.addToBattlefield(player2, new DuskLegionDreadnought());
        harness.setHand(player1, List.of(new ReefRoads()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findReefRoads(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("The new Pilot can saddle a Mount despite summoning sickness")
    void createsPilotThatCanSaddleMount() {
        addReefRoadsReady(player1);
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new GloryheathLynx());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Reef Roads");
        harness.assertInGraveyard(player1, "Reef Roads");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pilot"));
        harness.passBothPriorities();
        Permanent pilot = findPermanent(player1, "Pilot");
        assertThat(pilot.isSummoningSick()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pilot)).isEqualTo(1);
        assertThat(pilot.getCard().getColors()).isEmpty();
        assertThat(pilot.isTapped()).isFalse();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mount), null, null);
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(mount.isTapped()).isFalse();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
    }

    private Permanent addReefRoadsReady(Player player) {
        return addCreatureReady(player, new ReefRoads());
    }

    private Permanent findReefRoads(Player player) {
        return findPermanent(player, "Reef Roads");
    }
}
