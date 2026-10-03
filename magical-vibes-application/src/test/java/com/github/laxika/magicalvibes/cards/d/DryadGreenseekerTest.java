package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DryadGreenseeker.class, Forest.class, GreenwoodSentinel.class})
class DryadGreenseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Offers a top land for optional reveal and puts it into hand when accepted")
    void acceptsTopLand() {
        addReadyDryad();
        Card topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand, new GreenwoodSentinel()));

        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topLand);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topLand);
    }

    @Test
    @DisplayName("Declining the top land leaves it on top of the library")
    void declinesTopLand() {
        addReadyDryad();
        Card topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand, new GreenwoodSentinel()));

        activateAbility();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topLand);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topLand);
    }

    @Test
    @DisplayName("A nonland top card stays on top without offering a choice")
    void nonlandTopCardStaysOnTop() {
        addReadyDryad();
        Card topCard = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(topCard, new Forest()));

        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("The controller privately sees a nonland top card")
    void privatelyLooksAtNonland() {
        addReadyDryad();
        Card topCard = new GreenwoodSentinel();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        harness.clearMessages();

        activateAbility();

        assertThat(harness.getConn2().getMessagesContaining(topCard.getName())).isEmpty();
        assertThat(harness.getConn1().getMessagesContaining(topCard.getName())).isNotEmpty();
    }

    @Test
    @DisplayName("An empty library causes no choice or card movement")
    void emptyLibraryDoesNothing() {
        addReadyDryad();
        harness.setLibrary(player1, List.of());
        List<Card> originalHand = List.copyOf(gd.playerHands.get(player1.getId()));

        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(originalHand);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability resolves after the Dryad leaves the battlefield")
    void resolvesWithoutSource() {
        addReadyDryad();
        Card topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand, new GreenwoodSentinel()));

        harness.activateAbility(player1, 0, null, null);
        Card source = gd.playerBattlefields.get(player1.getId()).removeFirst().getCard();
        gd.playerGraveyards.get(player1.getId()).add(source);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topLand);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topLand);
    }

    private void addReadyDryad() {
        addCreatureReady(player1, new DryadGreenseeker());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void activateAbility() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
