package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinGuideTest extends BaseCardTest {

    private void declareAttack() {
        Permanent guide = new Permanent(new GoblinGuide());
        guide.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(guide);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Attacking puts a revealed land from the defending player's library into their hand")
    void landIsPutIntoDefendingPlayersHand() {
        Card land = new Forest();
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, nonland));

        declareAttack();

        assertThat(gd.playerHands.get(player2.getId())).contains(land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonland);
    }

    @Test
    @DisplayName("Attacking leaves a revealed nonland card on top of the defending player's library")
    void nonlandStaysOnTop() {
        Card nonland = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player2, List.of(nonland, land));

        declareAttack();

        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(nonland, land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonland, land);
    }

    @Test
    @DisplayName("An empty defending library causes no zone change")
    void emptyDefendingLibraryDoesNothing() {
        gd.playerDecks.get(player2.getId()).clear();
        int handSize = gd.playerHands.get(player2.getId()).size();

        declareAttack();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
