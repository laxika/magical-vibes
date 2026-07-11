package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WaterspoutWeaversTest extends BaseCardTest {

    @Test
    @DisplayName("Revealing the shared-type card gives flying to each creature you control")
    void revealGrantsFlyingToAll() {
        Permanent weavers = addCreatureReady(player1, new WaterspoutWeavers());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        setLibraryTop(new WaterspoutWeavers()); // shares Merfolk/Wizard

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(weavers.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The granted flying wears off at cleanup")
    void flyingWearsOffAtEndOfTurn() {
        Permanent weavers = addCreatureReady(player1, new WaterspoutWeavers());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        setLibraryTop(new WaterspoutWeavers());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(weavers.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal grants no flying")
    void decliningDoesNothing() {
        Permanent weavers = addCreatureReady(player1, new WaterspoutWeavers());
        setLibraryTop(new WaterspoutWeavers());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(weavers.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new WaterspoutWeavers());
        setLibraryTop(new GrizzlyBears()); // Bear — no shared type

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void setLibraryTop(Card card) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.add(card);
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
