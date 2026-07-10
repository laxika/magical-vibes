package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StorageMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing creature untaps only creatures")
    void chooseCreatureUntapsOnlyCreatures() {
        addReady(player1, new StorageMatrix());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        Permanent feather = addReady(player1, new AngelsFeather());
        bears.tap();
        forest.tap();
        feather.tap();

        advanceToNextTurn(player2);
        harness.handleListChoice(player1, "CREATURE");

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
        assertThat(feather.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing land untaps only lands")
    void chooseLandUntapsOnlyLands() {
        addReady(player1, new StorageMatrix());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        Permanent feather = addReady(player1, new AngelsFeather());
        bears.tap();
        forest.tap();
        feather.tap();

        advanceToNextTurn(player2);
        harness.handleListChoice(player1, "LAND");

        assertThat(forest.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
        assertThat(feather.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing artifact untaps only artifacts")
    void chooseArtifactUntapsOnlyArtifacts() {
        addReady(player1, new StorageMatrix());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        Permanent feather = addReady(player1, new AngelsFeather());
        bears.tap();
        forest.tap();
        feather.tap();

        advanceToNextTurn(player2);
        harness.handleListChoice(player1, "ARTIFACT");

        assertThat(feather.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Storage Matrix imposes no restriction — everything untaps normally")
    void tappedStorageMatrixImposesNoRestriction() {
        Permanent matrix = addReady(player1, new StorageMatrix());
        matrix.tap();
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        bears.tap();
        forest.tap();

        advanceToNextTurn(player2);

        // No type choice was presented; the untap step untapped everything, including the Matrix.
        assertThat(matrix.isTapped()).isFalse();
        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's untapped Storage Matrix restricts your untap step too")
    void opponentStorageMatrixRestrictsYourUntap() {
        addReady(player2, new StorageMatrix());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        bears.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleListChoice(player1, "CREATURE");

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn (advanceTurn)
    }
}
