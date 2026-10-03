package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.z.ZoeticGlyph;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirResponseUnit.class, BrightfieldGlider.class, ZoeticGlyph.class})
class AirResponseUnitTest extends BaseCardTest {

    @Test
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent vehicle = addCreatureReady(player1, new AirResponseUnit());
        Permanent crew = addCreatureReady(player1, new BrightfieldGlider());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addCreatureReady(player1, new AirResponseUnit());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent vehicle = addCreatureReady(player1, new AirResponseUnit());
        addCreatureReady(player1, new BrightfieldGlider());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    void summoningSickCreatureCanCrewAndCostIsPaidBeforeResolution() {
        Permanent vehicle = addCreatureReady(player1, new AirResponseUnit());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new BrightfieldGlider());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, vehicle)).isTrue();
    }

    @Test
    void tappedOrOpposingCreatureCannotCrew() {
        addCreatureReady(player1, new AirResponseUnit());
        Permanent tappedCrew = addCreatureReady(player1, new BrightfieldGlider());
        tappedCrew.tap();
        addCreatureReady(player2, new BrightfieldGlider());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewedVehicleAttacksWithoutTappingAndFliesOverGroundBlocker() {
        Permanent vehicle = addCreatureReady(player1, new AirResponseUnit());
        addCreatureReady(player1, new BrightfieldGlider());
        addCreatureReady(player2, new BrightfieldGlider());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(vehicle.isTapped()).isFalse();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void crewingDoesNotOverwriteEarlierBasePowerAndToughnessEffect() {
        Permanent vehicle = addCreatureReady(player1, new AirResponseUnit());
        addCreatureReady(player1, new BrightfieldGlider());
        harness.setHand(player1, List.of(new ZoeticGlyph()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, vehicle.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(4);
    }
}
