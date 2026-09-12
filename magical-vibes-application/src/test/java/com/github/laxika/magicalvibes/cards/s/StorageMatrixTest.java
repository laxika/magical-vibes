package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TitaniasSong;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StorageMatrix.class, GrizzlyBears.class, Forest.class, AngelsFeather.class, TitaniasSong.class})
class StorageMatrixTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing creature untaps only creatures")
    void chooseCreatureUntapsOnlyCreatures() {
        addCreatureReady(player1, new StorageMatrix());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent feather = addCreatureReady(player1, new AngelsFeather());
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
        addCreatureReady(player1, new StorageMatrix());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent feather = addCreatureReady(player1, new AngelsFeather());
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
        addCreatureReady(player1, new StorageMatrix());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent feather = addCreatureReady(player1, new AngelsFeather());
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
        Permanent matrix = addCreatureReady(player1, new StorageMatrix());
        matrix.tap();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = addCreatureReady(player1, new Forest());
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
        addCreatureReady(player2, new StorageMatrix());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = addCreatureReady(player1, new Forest());
        bears.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleListChoice(player1, "CREATURE");

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Storage Matrix that has lost its abilities imposes no restriction")
    void losingAbilitiesStopsRestriction() {
        addCreatureReady(player1, new StorageMatrix());
        addCreatureReady(player1, new TitaniasSong());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent forest = addCreatureReady(player1, new Forest());
        bears.tap();
        forest.tap();

        advanceToNextTurn(player2);

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        Player nextActivePlayer = currentActivePlayer.getId().equals(player1.getId()) ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }
}
