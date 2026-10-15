package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BatteredGolem.class, ConjurersBauble.class, BlindCreeper.class, Humility.class})
class BatteredGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Battered Golem does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();

        advanceToUpkeep(player1);

        assertThat(golem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An artifact entering triggers the may untap prompt")
    void artifactEnteringTriggersMayPrompt() {
        addReadyGolem(player1);
        castArtifactFor(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting untaps Battered Golem when an artifact enters")
    void acceptUntapsGolem() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        castArtifactFor(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining leaves Battered Golem tapped")
    void declineLeavesGolemTapped() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        castArtifactFor(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(golem.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control also triggers Battered Golem")
    void opponentArtifactEnteringTriggersMayPrompt() {
        addReadyGolem(player1);
        castArtifactFor(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("A non-artifact entering does not trigger Battered Golem")
    void nonArtifactDoesNotTrigger() {
        addReadyGolem(player1);
        harness.castFromHand(player1, new BlindCreeper(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Battered Golem triggers when it itself enters")
    void ownEntryTriggersUntap() {
        harness.castFromHand(player1, new BatteredGolem(), "{3}");
        harness.passBothPriorities();
        Permanent golem = findPermanent(player1, "Battered Golem");
        golem.tap();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(golem.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting an artifact does not trigger before the artifact enters")
    void castingArtifactDoesNotTrigger() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();

        harness.castFromHand(player1, new ConjurersBauble(), "{1}");

        assertThat(golem.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's artifact entry untaps only Battered Golem")
    void opponentArtifactUntapsOnlyGolem() {
        Permanent golem = addReadyGolem(player1);
        Permanent otherCreature = addCreatureReady(player1, new BlindCreeper());
        golem.tap();
        otherCreature.tap();
        castArtifactFor(player2);
        Permanent artifact = findPermanent(player2, "Conjurer's Bauble");
        artifact.tap();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(golem.isTapped()).isFalse();
        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Battered Golem untaps normally after losing all abilities")
    void losingAbilitiesRemovesUntapRestriction() {
        Permanent golem = addReadyGolem(player1);
        golem.tap();
        harness.castFromHand(player1, new Humility(), "{2}{W}{W}");
        resolveAllTriggers();

        advanceToUpkeep(player1);

        assertThat(golem.isTapped()).isFalse();
    }

    private Permanent addReadyGolem(Player player) {
        return addCreatureReady(player, new BatteredGolem());
    }

    private void castArtifactFor(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player, new ConjurersBauble(), "{1}");
        resolveAllTriggers();
    }
}
