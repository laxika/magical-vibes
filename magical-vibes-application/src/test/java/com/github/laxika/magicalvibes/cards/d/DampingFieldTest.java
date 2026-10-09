package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JourneyersKite;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WinterOrb;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DampingField.class, JourneyersKite.class, Forest.class, Ornithopter.class, WinterOrb.class})
class DampingFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Only one artifact untaps; non-artifacts untap normally")
    void onlyOneArtifactUntaps() {
        harness.addToBattlefield(player1, new DampingField());
        Permanent firstKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        Permanent secondKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        firstKite.tap();
        secondKite.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstKite.getId()));

        assertThat(firstKite.isTapped()).isFalse();
        assertThat(secondKite.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Damping Field still imposes the restriction")
    void tappedDampingFieldStillRestricts() {
        Permanent dampingField = harness.addToBattlefieldAndReturn(player1, new DampingField());
        Permanent firstKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        Permanent secondKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        dampingField.tap();
        firstKite.tap();
        secondKite.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstKite.getId()));

        assertThat(firstKite.isTapped()).isFalse();
        assertThat(secondKite.isTapped()).isTrue();
        assertThat(dampingField.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Damping Field restricts your untap step")
    void opponentDampingFieldRestricts() {
        harness.addToBattlefield(player2, new DampingField());
        Permanent firstKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        Permanent secondKite = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        firstKite.tap();
        secondKite.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstKite.getId()));

        assertThat(firstKite.isTapped()).isFalse();
        assertThat(secondKite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The player may choose to untap no artifacts")
    void mayUntapNoArtifacts() {
        harness.addToBattlefield(player1, new DampingField());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        first.tap();
        second.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A single tapped artifact untaps normally even with another untapped artifact")
    void singleTappedArtifactUntaps() {
        harness.addToBattlefield(player1, new DampingField());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        tapped.tap();

        advanceToNextTurn(player2);

        assertThat(tapped.isTapped()).isFalse();
        assertThat(untapped.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Multiple Damping Fields still allow one artifact to untap")
    void multipleCopiesDoNotReduceLimit() {
        harness.addToBattlefield(player1, new DampingField());
        harness.addToBattlefield(player1, new DampingField());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        first.tap();
        second.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Damping Field still restricts artifacts when Winter Orb's land choice is processed first")
    void dampingFieldAndWinterOrbBothRestrictUntapping() {
        harness.addToBattlefield(player1, new WinterOrb());
        harness.addToBattlefield(player1, new DampingField());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        firstArtifact.tap();
        secondArtifact.tap();
        firstLand.tap();
        secondLand.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId()));
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMultiplePermanentsChosen(player1, List.of(firstArtifact.getId()));
        }

        assertThat(firstLand.isTapped()).isFalse();
        assertThat(secondLand.isTapped()).isTrue();
        assertThat(firstArtifact.isTapped()).isFalse();
        assertThat(secondArtifact.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.UNTAP,
                () -> harness.passUntilWithNoAttackers(newActivePlayer, TurnStep.UNTAP));
    }
}
