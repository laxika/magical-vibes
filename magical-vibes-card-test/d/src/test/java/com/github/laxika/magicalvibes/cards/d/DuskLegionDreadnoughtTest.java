package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NestRobber;
import com.github.laxika.magicalvibes.cards.s.SailorOfMeans;
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

@CardUsed({DuskLegionDreadnought.class, NestRobber.class, SailorOfMeans.class})
class DuskLegionDreadnoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Dreadnought is not a creature before crewing")
    void notACreatureBeforeCrew() {
        Permanent dreadnought = addDreadnoughtReady(player1);

        assertThat(gqs.isCreature(gd, dreadnought)).isFalse();
    }

    @Test
    @DisplayName("Crewing with a creature of power >= 2 animates Dreadnought")
    void crewWithSufficientPower() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        Permanent crew = addCreatureReady(player1, new NestRobber());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadnought.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();
        assertThat(gqs.getEffectivePower(gd, dreadnought)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dreadnought)).isEqualTo(6);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutEnoughPower() {
        addDreadnoughtReady(player1);
        // No creatures at all
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Crew animation resets at end of turn")
    void crewResetsAtEndOfTurn() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        addCreatureReady(player1, new NestRobber());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(dreadnought.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, dreadnought)).isFalse();
    }

    @Test
    @DisplayName("Crew does not require the vehicle to tap")
    void crewDoesNotTapVehicle() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        addCreatureReady(player1, new NestRobber());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadnought.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A crewed Dreadnought attacks without tapping and deals four damage")
    void attacksWithVigilance() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        addCreatureReady(player1, new NestRobber());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(dreadnought.isTapped()).isFalse();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Two summoning-sick one-power creatures can pay crew two")
    void multipleSummoningSickCreaturesCanCrew() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SailorOfMeans());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SailorOfMeans());
        first.setSummoningSick(true);
        second.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, dreadnought)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures cannot pay crew")
    void tappedCreatureCannotCrew() {
        addDreadnoughtReady(player1);
        Permanent crew = addCreatureReady(player1, new NestRobber());
        crew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Opponent's creatures cannot pay crew")
    void opponentsCreatureCannotCrew() {
        addDreadnoughtReady(player1);
        Permanent opposingCreature = addCreatureReady(player2, new NestRobber());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(opposingCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("One power is insufficient for crew two")
    void onePowerCannotCrew() {
        addDreadnoughtReady(player1);
        Permanent crew = addCreatureReady(player1, new SailorOfMeans());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(crew.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped vehicle can be crewed without untapping it")
    void tappedVehicleCanBeCrewed() {
        Permanent dreadnought = addDreadnoughtReady(player1);
        dreadnought.tap();
        addCreatureReady(player1, new NestRobber());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, dreadnought)).isTrue();
        assertThat(dreadnought.isTapped()).isTrue();
    }

    private Permanent addDreadnoughtReady(Player player) {
        return addCreatureReady(player, new DuskLegionDreadnought());
    }

}
