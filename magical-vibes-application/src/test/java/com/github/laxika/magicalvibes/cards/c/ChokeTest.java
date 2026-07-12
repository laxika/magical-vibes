package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChokeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Island does not untap while Choke is out")
    void islandStaysTapped() {
        addReady(player1, new Choke());
        Permanent island = addReady(player1, new Island());
        island.tap();

        advanceToNextTurn(player2);

        assertThat(island.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-Island land untaps normally")
    void forestUntaps() {
        addReady(player1, new Choke());
        Permanent forest = addReady(player1, new Forest());
        forest.tap();

        advanceToNextTurn(player2);

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Affects opponents' Islands during their untap step")
    void affectsOpponentIslands() {
        addReady(player1, new Choke());
        Permanent opponentIsland = addReady(player2, new Island());
        opponentIsland.tap();

        // player2's untap step
        advanceToNextTurn(player1);

        assertThat(opponentIsland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once Choke leaves, Islands untap again")
    void untapsAfterChokeLeaves() {
        Permanent choke = addReady(player1, new Choke());
        Permanent island = addReady(player1, new Island());
        island.tap();

        gd.playerBattlefields.get(player1.getId()).remove(choke);

        advanceToNextTurn(player2);

        assertThat(island.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
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
