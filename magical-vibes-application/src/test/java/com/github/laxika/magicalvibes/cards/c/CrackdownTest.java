package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CrackdownTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped nonwhite creature with power 3 or greater does not untap")
    void power3NonwhiteCreatureStaysTapped() {
        addReady(player1, new Crackdown());
        Permanent giant = addReady(player1, new HillGiant());
        giant.tap();

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped nonwhite creature with power under 3 untaps normally")
    void power2NonwhiteCreatureUntaps() {
        addReady(player1, new Crackdown());
        Permanent bears = addReady(player1, new GrizzlyBears());
        bears.tap();

        advanceToNextTurn(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped white creature with power 3 or greater untaps normally")
    void whiteCreatureUntaps() {
        addReady(player1, new Crackdown());
        Permanent angel = addReady(player1, new SerraAngel());
        angel.tap();

        advanceToNextTurn(player2);

        assertThat(angel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Crackdown affects nonwhite creatures during an opponent's untap step")
    void affectsOpponentCreatures() {
        addReady(player1, new Crackdown());
        Permanent opponentGiant = addReady(player2, new HillGiant());
        opponentGiant.tap();

        advanceToNextTurn(player1);

        assertThat(opponentGiant.isTapped()).isTrue();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
