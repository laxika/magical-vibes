package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DuneDrifter;
import com.github.laxika.magicalvibes.cards.g.GuidelightMatrix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FangGuardian.class, DuneDrifter.class, GuidelightMatrix.class})
class FangGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives another creature you control +2/+2")
    void etbBoostsAnotherCreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FangGuardian());
        castFangGuardian(creature);

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB can target a Vehicle you control")
    void etbBoostsVehicleYouControl() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuneDrifter());
        castFangGuardian(vehicle);

        assertThat(vehicle.getPowerModifier()).isEqualTo(2);
        assertThat(vehicle.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void etbBoostWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FangGuardian());
        castFangGuardian(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FangGuardian());
        harness.setHand(player1, List.of(new FangGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature or Vehicle you control");
    }

    @Test
    @DisplayName("Cannot target Fang Guardian itself")
    void cannotTargetItself() {
        harness.setHand(player1, List.of(new FangGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fang Guardian");
        Permanent guardian = findPermanent(player1, "Fang Guardian");
        assertThat(guardian.getPowerModifier()).isZero();
        assertThat(guardian.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBeCastDuringOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FangGuardian());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        castFangGuardian(creature);

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(countPermanents(player1, "Fang Guardian")).isEqualTo(2);
    }

    @Test
    void cannotTargetNoncreatureNonvehicleArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GuidelightMatrix());
        harness.setHand(player1, List.of(new FangGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature or Vehicle you control");
    }

    @Test
    void cannotTargetOpponentsVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new DuneDrifter());
        harness.setHand(player1, List.of(new FangGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, vehicle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature or Vehicle you control");
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FangGuardian());
        harness.setHand(player1, List.of(new FangGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        Permanent source = findPermanents(player1, "Fang Guardian").stream()
                .filter(permanent -> !permanent.getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        resolveAllTriggers();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void abilityDoesNotBoostTargetThatChangesController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FangGuardian());
        harness.setHand(player1, List.of(new FangGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        resolveAllTriggers();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void castFangGuardian(Permanent target) {
        harness.setHand(player1, List.of(new FangGuardian()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
    }
}
