package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameOfAnor.class, FugitiveWizard.class, FountainOfYouth.class, GrizzlyBears.class,
        Forest.class, Mountain.class})
class FlameOfAnorTest extends BaseCardTest {

    @Test
    void targetPlayerDrawsTwoCards() {
        harness.setLibrary(player2, List.of(new Forest(), new Mountain()));
        cast(new int[]{0}, List.of(player2.getId()));

        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player2, "Mountain");
    }

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        cast(new int[]{1}, List.of(artifact.getId()));

        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void dealsFiveDamageToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{2}, List.of(creature.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void wizardAllowsChoosingTwoModes() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(new int[]{1, 2}, List.of(artifact.getId(), creature.getId()));

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotChooseTwoModesWithoutAWizard() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1, 2}, List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactModeRejectsCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    void dealsExactlyFiveDamageToASurvivingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        cast(new int[]{2}, List.of(creature.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(creature.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void wizardAllowsDrawingAndDestroyingAnArtifact() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.setLibrary(player1, List.of(new Forest(), new Mountain()));
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        cast(new int[]{0, 1}, List.of(player1.getId(), artifact.getId()));

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void wizardAllowsDrawingAndDamagingACreature() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.setLibrary(player2, List.of(new Forest(), new Mountain()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0, 2}, List.of(player2.getId(), creature.getId()));

        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player2, "Mountain");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void controllingAWizardStillAllowsChoosingOnlyOneMode() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{2}, List.of(creature.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
    }

    @Test
    void opponentsWizardDoesNotAllowChoosingTwoModes() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1, 2}, List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chosenModesStillResolveAfterWizardLeavesBattlefield() {
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();
        harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1, 2}, List.of(artifact.getId(), creature.getId()));

        harness.getPermanentRemovalService().removePermanentToHand(gd, wizard);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void remainingModeResolvesWhenOneTargetLeavesBattlefield() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();
        harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1, 2}, List.of(artifact.getId(), creature.getId()));

        harness.getPermanentRemovalService().removePermanentToHand(gd, artifact);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void creatureModeRejectsArtifactTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{2}, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        prepareSpell();
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new FlameOfAnor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
