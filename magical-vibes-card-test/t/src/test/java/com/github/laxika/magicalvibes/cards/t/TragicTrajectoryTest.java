package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KnightLuminary;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({TragicTrajectory.class, FountainOfYouth.class, GrizzlyBears.class, HillGiant.class,
        Shock.class, Boomerang.class, KnightLuminary.class, Plains.class})
class TragicTrajectoryTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -2/-2 without Void")
    void givesMinusTwoMinusTwoWithoutVoid() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castTragicTrajectory(target);

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives target creature -10/-10 with Void")
    void givesMinusTenMinusTenWithVoid() {
        Permanent creatureToKill = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock(), new TragicTrajectory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, creatureToKill.getId());
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Checks Void when the spell resolves")
    void checksVoidAtResolution() {
        Permanent creatureToKill = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock(), new TragicTrajectory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 1, target.getId());
        harness.castInstant(player1, 0, creatureToKill.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The temporary reduction wears off at end of turn")
    void reductionWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castTragicTrajectory(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new TragicTrajectory()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A spell warped this turn enables Void without any permanent leaving")
    void warpedSpellEnablesVoid() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new KnightLuminary(), new TragicTrajectory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Knight Luminary");
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-10);
        assertThat(target.getToughnessModifier()).isEqualTo(-10);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Returning a noncreature artifact to hand enables Void")
    void returningArtifactEnablesVoid() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Boomerang(), new TragicTrajectory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, artifact.getId());
        harness.assertInHand(player1, "Fountain of Youth");
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-10);
        assertThat(target.getToughnessModifier()).isEqualTo(-10);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Returning only a land to hand does not enable Void")
    void returningLandDoesNotEnableVoid() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Boomerang(), new TragicTrajectory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, land.getId());
        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Can target your own creature and put it in the graveyard at zero toughness")
    void killsOwnCreatureWithoutVoid() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castTragicTrajectory(target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not affect another creature when its target leaves before resolution")
    void removedTargetMakesSpellDoNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TragicTrajectory(), new Boomerang()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        harness.assertInHand(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Tragic Trajectory");
    }

    private void castTragicTrajectory(Permanent target) {
        harness.setHand(player1, List.of(new TragicTrajectory()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }
}
