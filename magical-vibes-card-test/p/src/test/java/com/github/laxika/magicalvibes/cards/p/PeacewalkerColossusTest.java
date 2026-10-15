package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeacewalkerColossus.class, DuskLegionDreadnought.class, GrizzlyBears.class})
class PeacewalkerColossusTest extends BaseCardTest {

    @Test
    void animatesAnotherVehicleYouControlUntilEndOfTurn() {
        Permanent colossus = addReadyPeacewalkerColossus(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        addAnimationMana();

        harness.activateAbility(player1, 0, 0, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.isArtifact(gd, vehicle)).isTrue();
        assertThat(gqs.isCreature(gd, colossus)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
    }

    @Test
    void cannotAnimateItselfOrAnOpponentVehicle() {
        Permanent colossus = addReadyPeacewalkerColossus(player1);
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        addAnimationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, colossus.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Vehicle you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentVehicle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Vehicle you control");
    }

    @Test
    void crewsByTappingCreaturesWithTotalPowerAtLeastFour() {
        Permanent colossus = addReadyPeacewalkerColossus(player1);
        Permanent firstCrew = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCrew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, colossus)).isTrue();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addReadyPeacewalkerColossus(player1);
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void canAnimateAnotherVehicleWhileTappedAndSummoningSick() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new PeacewalkerColossus());
        colossus.tap();
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new PeacewalkerColossus());
        addAnimationMana();

        harness.activateAbility(player1, 0, 0, null, vehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(colossus.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, colossus)).isFalse();
    }

    @Test
    void cannotAnimateANonVehicleCreature() {
        addReadyPeacewalkerColossus(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addAnimationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Vehicle you control");
    }

    @Test
    void animationResolvesEvenAfterItsSourceLeavesTheBattlefield() {
        Permanent colossus = addReadyPeacewalkerColossus(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new PeacewalkerColossus());
        addAnimationMana();

        harness.activateAbility(player1, 0, 0, null, vehicle.getId());
        gd.playerBattlefields.get(player1.getId()).remove(colossus);
        gd.playerGraveyards.get(player1.getId()).add(colossus.getCard());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    @Test
    void crewsWithSummoningSickCreaturesAndAnimationExpiresAtEndOfTurn() {
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new PeacewalkerColossus());
        Permanent firstCrew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCrew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, colossus)).isTrue();
        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(colossus.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, colossus)).isFalse();
    }

    @Test
    void tappedCreaturesCannotPayCrewCost() {
        addReadyPeacewalkerColossus(player1);
        Permanent firstCrew = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        firstCrew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void animationDoesNotResolveIfTheTargetChangesController() {
        addReadyPeacewalkerColossus(player1);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new PeacewalkerColossus());
        addAnimationMana();

        harness.activateAbility(player1, 0, 0, null, vehicle.getId());
        gd.playerBattlefields.get(player1.getId()).remove(vehicle);
        gd.playerBattlefields.get(player2.getId()).add(vehicle);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentCreaturesCannotPayCrewCost() {
        addReadyPeacewalkerColossus(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    private Permanent addReadyPeacewalkerColossus(Player player) {
        return addCreatureReady(player, new PeacewalkerColossus());
    }

    private void addAnimationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
