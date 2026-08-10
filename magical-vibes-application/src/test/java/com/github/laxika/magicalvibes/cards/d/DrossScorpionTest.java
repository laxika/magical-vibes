package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DrossScorpionTest extends BaseCardTest {

    @Test
    @DisplayName("When Dross Scorpion dies, it may untap a target artifact")
    void selfDeathMayUntapTargetArtifact() {
        harness.addToBattlefield(player1, new DrossScorpion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        artifact.tap();

        killDrossScorpion();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("When another artifact creature dies, Dross Scorpion may untap a target artifact")
    void anotherArtifactCreatureDeathMayUntapTargetArtifact() {
        harness.addToBattlefield(player1, new DrossScorpion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        artifact.tap();
        harness.addToBattlefield(player2, new Memnite());

        destroyOneCreatureControlledByPlayer2();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Dross Scorpion does not trigger when a non-artifact creature dies")
    void doesNotTriggerForNonArtifactCreature() {
        harness.addToBattlefield(player1, new DrossScorpion());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyOneCreatureControlledByPlayer2();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void killDrossScorpion() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.RED, 6);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Dross Scorpion"));
        harness.passBothPriorities();
    }

    private void destroyOneCreatureControlledByPlayer2() {
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.c.CruelEdict()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
