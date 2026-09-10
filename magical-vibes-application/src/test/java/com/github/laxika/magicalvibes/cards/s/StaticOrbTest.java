package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaticOrb.class, LowlandGiant.class, Forest.class, LotusPetal.class})
class StaticOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Only the two chosen permanents untap; the rest stay tapped")
    void picksTwoOfThreeToUntap() {
        addCreatureReady(player1, new StaticOrb());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        giant.tap();
        forest.tap();
        petal.tap();

        advanceToNextTurn(player2);
        harness.clearMessages();
        harness.handleMultiplePermanentsChosen(player1, List.of(giant.getId(), forest.getId()));

        assertThat(giant.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(petal.isTapped()).isTrue();
        assertThat(harness.getConn1().getMessagesContaining("\"type\":\"GAME_STATE\"")).hasSize(1);
        assertThat(harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"")).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Static Orb imposes no restriction — everything untaps normally")
    void tappedStaticOrbImposesNoRestriction() {
        Permanent orb = addCreatureReady(player1, new StaticOrb());
        orb.tap();
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        giant.tap();
        forest.tap();
        petal.tap();

        advanceToNextTurn(player2);

        // No choice was presented; the untap step untapped everything, including the Orb.
        assertThat(orb.isTapped()).isFalse();
        assertThat(giant.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(petal.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two or fewer permanents untap normally without a choice")
    void twoOrFewerUntapNormally() {
        addCreatureReady(player1, new StaticOrb());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        giant.tap();
        forest.tap();

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller may choose no permanents to untap")
    void mayChooseNoPermanentsToUntap() {
        addCreatureReady(player1, new StaticOrb());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        giant.tap();
        forest.tap();
        petal.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(giant.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
        assertThat(petal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's untapped Static Orb restricts your untap step too")
    void opponentStaticOrbRestrictsYourUntap() {
        addCreatureReady(player2, new StaticOrb());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        giant.tap();
        forest.tap();
        petal.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(giant.getId()));

        assertThat(giant.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
        assertThat(petal.isTapped()).isTrue();
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
