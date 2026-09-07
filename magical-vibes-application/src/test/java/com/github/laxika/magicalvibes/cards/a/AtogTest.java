package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Atog.class, GrizzlyBears.class, Ornithopter.class})
class AtogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives Atog +2/+2 until end of turn")
    void sacrificeBoostsAtog() {
        Permanent atog = harness.addToBattlefieldAndReturn(player1, new Atog());
        harness.addToBattlefield(player1, new Ornithopter());
        int powerBefore = gqs.getEffectivePower(gd, atog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, atog);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Artifact was sacrificed
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(gqs.getEffectivePower(gd, atog)).isEqualTo(powerBefore + 2);
        assertThat(gqs.getEffectiveToughness(gd, atog)).isEqualTo(toughnessBefore + 2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent atog = harness.addToBattlefieldAndReturn(player1, new Atog());
        harness.addToBattlefield(player1, new Ornithopter());
        int powerBefore = gqs.getEffectivePower(gd, atog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, atog);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, atog)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, atog)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new Atog());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("Cannot sacrifice an artifact controlled by an opponent")
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new Atog());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("Chooses which artifact to sacrifice when multiple are available")
    void choosesArtifactToSacrifice() {
        Permanent atog = harness.addToBattlefieldAndReturn(player1, new Atog());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        int powerBefore = gqs.getEffectivePower(gd, atog);
        int toughnessBefore = gqs.getEffectiveToughness(gd, atog);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstArtifact.getId(), secondArtifact.getId());

        harness.handlePermanentChosen(player1, secondArtifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondArtifact.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstArtifact);
        assertThat(gqs.getEffectivePower(gd, atog)).isEqualTo(powerBefore + 2);
        assertThat(gqs.getEffectiveToughness(gd, atog)).isEqualTo(toughnessBefore + 2);
    }
}
