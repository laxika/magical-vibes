package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

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
