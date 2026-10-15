package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({MonoistCircuitFeeder.class, AirElemental.class, FountainOfYouth.class, GrizzlyBears.class})
class MonoistCircuitFeederTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creature and weakens an opponent's creature by your artifact count")
    void modifiesBothTargetsByControlledArtifactCount() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new MonoistCircuitFeeder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        resolveAllTriggers();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(4);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The temporary modifications wear off at cleanup")
    void modificationsWearOffAtCleanup() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player1, new FountainOfYouth());

        harness.setHand(player1, List.of(new MonoistCircuitFeeder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
        assertThat(opposingCreature.getPowerModifier()).isZero();
        assertThat(opposingCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Requires the first target to be a creature you control")
    void firstTargetMustBeControlledCreature() {
        Permanent firstOpposingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent secondOpposingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new MonoistCircuitFeeder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castCreature(
                player1, 0, List.of(firstOpposingCreature.getId(), secondOpposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Requires the second target to be a creature an opponent controls")
    void secondTargetMustBeOpposingCreature() {
        Permanent firstOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new MonoistCircuitFeeder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castCreature(
                player1, 0, List.of(firstOwnCreature.getId(), secondOwnCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    void canTargetItselfAfterEntering() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MonoistCircuitFeeder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent feeder = findPermanent(player1, "Monoist Circuit-Feeder");
        harness.handlePermanentChosen(player1, feeder.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        resolveAllTriggers();

        assertThat(feeder.getEffectivePower()).isEqualTo(5);
        assertThat(feeder.getEffectiveToughness()).isEqualTo(4);
        assertThat(opposingCreature.getEffectivePower()).isEqualTo(4);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void artifactCountIsDeterminedAtResolutionAndThenStaysFixed() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MonoistCircuitFeeder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        resolveAllTriggers();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new FountainOfYouth());

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void stillWeakensOpponentWhenSourceAndFirstTargetLeave() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MonoistCircuitFeeder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent feeder = findPermanent(player1, "Monoist Circuit-Feeder");
        harness.handlePermanentChosen(player1, feeder.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(feeder);
        resolveAllTriggers();

        assertThat(opposingCreature.getEffectivePower()).isEqualTo(4);
        assertThat(opposingCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(feeder.getPowerModifier()).isZero();
    }

    @Test
    void stillBoostsOwnCreatureWhenOpposingTargetLeaves() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MonoistCircuitFeeder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        resolveAllTriggers();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void zeroArtifactsAtResolutionProducesNoModifiers() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MonoistCircuitFeeder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Monoist Circuit-Feeder"));
        resolveAllTriggers();

        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(opposingCreature.getToughnessModifier()).isZero();
    }

    @Test
    void opponentCreatureDiesFromZeroToughnessWhileOwnCreatureGetsFullBoost() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MonoistCircuitFeeder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0, List.of(ownCreature.getId(), opposingCreature.getId()));
        resolveAllTriggers();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCreature.getCard());
    }

    @Test
    void canEnterWithoutAnOpposingCreatureAndDoesNotApplyHalfTheAbility() {
        harness.setHand(player1, List.of(new MonoistCircuitFeeder()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent feeder = findPermanent(player1, "Monoist Circuit-Feeder");
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, feeder.getId());
        }
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(feeder.getPowerModifier()).isZero();
        assertThat(feeder.getToughnessModifier()).isZero();
    }
}
