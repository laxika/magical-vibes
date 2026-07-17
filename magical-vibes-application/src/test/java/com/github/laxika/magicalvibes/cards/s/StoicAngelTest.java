package com.github.laxika.magicalvibes.cards.s;

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

class StoicAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Only the one chosen creature untaps; other creatures stay tapped, non-creatures untap normally")
    void picksOneCreatureLandsUntapFreely() {
        addReady(player1, new StoicAngel());
        Permanent bears1 = addReady(player1, new GrizzlyBears());
        Permanent bears2 = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        bears1.tap();
        bears2.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears1.getId()));

        assertThat(bears1.isTapped()).isFalse();
        assertThat(bears2.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("One tapped creature untaps normally without a choice")
    void oneCreatureUntapsNormally() {
        addReady(player1, new StoicAngel());
        Permanent bears = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        bears.tap();
        forest.tap();

        advanceToNextTurn(player2);

        assertThat(bears.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Stoic Angel restricts your untap step too")
    void opponentStoicAngelRestrictsYourUntap() {
        addReady(player2, new StoicAngel());
        Permanent bears1 = addReady(player1, new GrizzlyBears());
        Permanent bears2 = addReady(player1, new GrizzlyBears());
        Permanent forest = addReady(player1, new Forest());
        bears1.tap();
        bears2.tap();
        forest.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears1.getId()));

        assertThat(bears1.isTapped()).isFalse();
        assertThat(bears2.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The lock applies even while Stoic Angel itself is tapped (unlike Static Orb)")
    void appliesWhileAngelTapped() {
        Permanent angel = addReady(player1, new StoicAngel());
        angel.tap();
        Permanent bears = addReady(player1, new GrizzlyBears());
        bears.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(bears.isTapped()).isFalse();
        assertThat(angel.isTapped()).isTrue();
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
