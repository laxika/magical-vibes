package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
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

@CardUsed({ShipwreckSentry.class, ChromaticStar.class, GrizzlyBears.class})
class ShipwreckSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack before an artifact enters under its controller's control")
    void cannotAttackBeforeArtifactEnters() {
        Permanent sentry = addCreatureReady(player1, new ShipwreckSentry());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(sentry.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Can attack after an artifact enters under its controller's control")
    void canAttackAfterArtifactEnters() {
        Permanent sentry = addCreatureReady(player1, new ShipwreckSentry());
        harness.setHand(player1, List.of(new ChromaticStar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(sentry.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An artifact entering under an opponent's control does not help")
    void opponentArtifactDoesNotHelp() {
        Permanent sentry = addCreatureReady(player1, new ShipwreckSentry());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ChromaticStar()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(sentry.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("A nonartifact permanent entering under its controller's control does not help")
    void nonartifactDoesNotHelp() {
        Permanent sentry = addCreatureReady(player1, new ShipwreckSentry());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(sentry.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("An artifact that entered before Sentry still grants permission to attack")
    void artifactEnteringBeforeSentryCounts() {
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());
        Permanent sentry = addCreatureReady(player1, new ShipwreckSentry());

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThat(sentry.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An artifact leaving the battlefield does not revoke permission to attack")
    void artifactLeavingStillCounts() {
        Permanent sentry = addCreatureReady(player1, new ShipwreckSentry());
        Permanent artifact = harness.enterBattlefieldAndReturn(player1, new ChromaticStar());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, artifact));
        harness.assertNotOnBattlefield(player1, "Chromatic Star");

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(sentry.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("An artifact entering on a previous turn does not grant permission to attack")
    void previousTurnArtifactDoesNotCount() {
        Permanent sentry = addCreatureReady(player1, new ShipwreckSentry());
        harness.enterBattlefieldAndReturn(player1, new ChromaticStar());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
        assertThat(sentry.isAttacking()).isFalse();
    }
}
