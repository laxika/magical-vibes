package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MomosHeist.class, Ornithopter.class, GrizzlyBears.class, Stifle.class, RodOfRuin.class, Unsummon.class})
class MomosHeistTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of, untaps, and grants haste to the target artifact")
    void resolvesArtifactHeist() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        artifact.tap();

        cast(artifact);

        assertThat(artifact.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Sacrifices the artifact at the beginning of the next end step")
    void sacrificesArtifactAtNextEndStep() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        cast(artifact);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Only targets artifacts")
    void requiresArtifactTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MomosHeist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Can target and sacrifice an artifact already controlled by the caster")
    void canTargetOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();

        cast(artifact);

        assertThat(artifact.isTapped()).isFalse();
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Countering the sacrifice trigger preserves control and haste across cleanup")
    void retainsControlAndHasteWhenSacrificeTriggerIsCountered() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        cast(artifact);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player1, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getTargetableId());
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        assertThat(artifact.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @DisplayName("The original caster cannot sacrifice an artifact taken by another player")
    void cannotSacrificeArtifactAfterLosingControl() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        cast(artifact);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new MomosHeist()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player2, 0, artifact.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).extracting(StackEntry::getControllerId)
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());

        StackEntry secondCastersTrigger = gd.stack.stream()
                .filter(entry -> entry.getControllerId().equals(player2.getId()))
                .findFirst().orElseThrow();
        harness.setHand(player2, List.of(new Stifle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, secondCastersTrigger.getTargetableId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Steals, untaps, and sacrifices a noncreature artifact")
    void canTargetNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());
        artifact.tap();

        cast(artifact);

        harness.assertOnBattlefield(player1, "Rod of Ruin");
        harness.assertNotOnBattlefield(player2, "Rod of Ruin");
        assertThat(artifact.isTapped()).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Rod of Ruin");
        harness.assertInGraveyard(player2, "Rod of Ruin");
    }

    @Test
    @DisplayName("Does not affect an artifact that leaves before resolution")
    void doesNotAffectArtifactThatLeavesBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, List.of(new MomosHeist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, artifact.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.assertInHand(player2, "Ornithopter");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInHand(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Momo's Heist");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Ornithopter");
    }

    private void cast(Permanent artifact) {
        harness.setHand(player1, List.of(new MomosHeist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, artifact.getId());
    }
}
