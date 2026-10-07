package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TigerSeal.class})
class TigerSealTest extends BaseCardTest {

    @Test
    @DisplayName("Taps itself at the beginning of its controller's upkeep")
    void tapsAtBeginningOfUpkeep() {
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new TigerSeal());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(seal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps itself when its controller draws their second card")
    void untapsOnSecondCardDraw() {
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new TigerSeal());
        seal.tap();
        harness.setLibrary(player1, java.util.List.of(new TigerSeal(), new TigerSeal(), new TigerSeal()));

        draw(player1.getId());
        assertThat(seal.isTapped()).isTrue();

        draw(player1.getId());
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();

        assertThat(seal.isTapped()).isFalse();

        draw(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not tap during its opponent's upkeep")
    void doesNotTapDuringOpponentsUpkeep() {
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new TigerSeal());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(seal.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent draws do not untap it")
    void opponentDrawsDoNotUntapIt() {
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new TigerSeal());
        seal.tap();
        harness.setLibrary(player2, java.util.List.of(new TigerSeal(), new TigerSeal()));

        draw(player2.getId());
        draw(player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(seal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Controller's second draw untaps it during an opponent's turn")
    void untapsDuringOpponentsTurn() {
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new TigerSeal());
        seal.tap();
        harness.forceActivePlayer(player2);
        harness.setLibrary(player1, java.util.List.of(new TigerSeal(), new TigerSeal()));

        draw(player1.getId());
        draw(player1.getId());

        assertThat(seal.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();
        assertThat(seal.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Counts the first draw even when it occurred before this creature entered")
    void countsDrawBeforeEntering() {
        harness.setLibrary(player1, java.util.List.of(new TigerSeal(), new TigerSeal()));
        draw(player1.getId());
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new TigerSeal());
        seal.tap();

        draw(player1.getId());

        assertThat(gd.stack).hasSize(1);
        resolveTopOfStack();
        assertThat(seal.isTapped()).isFalse();
    }
    private void draw(java.util.UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }

    private void resolveTopOfStack() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
