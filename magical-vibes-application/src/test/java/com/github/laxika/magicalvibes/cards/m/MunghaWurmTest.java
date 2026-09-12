package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HollowWarrior;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MunghaWurm.class, RhysticCave.class, HollowWarrior.class})
class MunghaWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Only one land untaps; nonlands untap normally")
    void picksOneLandToUntapNonlandsUntapNormally() {
        addCreatureReady(player1, new MunghaWurm());
        Permanent firstLand = addCreatureReady(player1, new RhysticCave());
        Permanent secondLand = addCreatureReady(player1, new RhysticCave());
        Permanent warrior = addCreatureReady(player1, new HollowWarrior());
        firstLand.tap();
        secondLand.tap();
        warrior.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId()));

        assertThat(firstLand.isTapped()).isFalse();
        assertThat(secondLand.isTapped()).isTrue();
        assertThat(warrior.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The active player may choose not to untap any land")
    void mayChooseNoLandToUntap() {
        addCreatureReady(player1, new MunghaWurm());
        Permanent firstLand = addCreatureReady(player1, new RhysticCave());
        Permanent secondLand = addCreatureReady(player1, new RhysticCave());
        firstLand.tap();
        secondLand.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(firstLand.isTapped()).isTrue();
        assertThat(secondLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The restriction applies even when Mungha Wurm starts tapped")
    void tappedMunghaWurmStillRestricts() {
        Permanent wurm = addCreatureReady(player1, new MunghaWurm());
        wurm.tap();
        Permanent firstLand = addCreatureReady(player1, new RhysticCave());
        Permanent secondLand = addCreatureReady(player1, new RhysticCave());
        firstLand.tap();
        secondLand.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId()));

        assertThat(firstLand.isTapped()).isFalse();
        assertThat(secondLand.isTapped()).isTrue();
        assertThat(wurm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Mungha Wurm controlled by an opponent restricts your lands")
    void opponentMunghaWurmRestrictsYourLands() {
        addCreatureReady(player2, new MunghaWurm());
        Permanent firstLand = addCreatureReady(player1, new RhysticCave());
        Permanent secondLand = addCreatureReady(player1, new RhysticCave());
        firstLand.tap();
        secondLand.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstLand.getId()));

        assertThat(firstLand.isTapped()).isFalse();
        assertThat(secondLand.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }
}
