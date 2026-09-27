package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmarethTheLustrous.class, GrizzlyBears.class, Shock.class})
class AmarethTheLustrousTest extends BaseCardTest {

    @Test
    @DisplayName("A permanent you control with a shared card type offers the top card for hand")
    void matchingCardMayBePutIntoHand() {
        Card topCard = new GrizzlyBears();
        Card below = new Shock();
        enterAnotherPermanentWithLibrary(new GrizzlyBears(), topCard, below);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(below);
    }

    @Test
    @DisplayName("Declining the matching top card leaves it on top")
    void decliningMatchingCardLeavesItOnTop() {
        Card topCard = new GrizzlyBears();
        Card below = new Shock();
        enterAnotherPermanentWithLibrary(new GrizzlyBears(), topCard, below);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, below);
    }

    @Test
    @DisplayName("A top card without a shared card type stays on top without a choice")
    void nonmatchingCardStaysOnTop() {
        Card topCard = new Shock();
        Card below = new GrizzlyBears();
        enterAnotherPermanentWithLibrary(new GrizzlyBears(), topCard, below);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, below);
    }

    @Test
    @DisplayName("A permanent an opponent controls does not trigger Amareth")
    void opponentPermanentDoesNotTrigger() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new AmarethTheLustrous());
        harness.setLibrary(player1, List.of(topCard));

        harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Amareth does not trigger for its own entry")
    void doesNotTriggerForOwnEntry() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.enterBattlefieldAndReturn(player1, new AmarethTheLustrous());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private void enterAnotherPermanentWithLibrary(Card enteringPermanent, Card topCard, Card below) {
        harness.addToBattlefield(player1, new AmarethTheLustrous());
        harness.setLibrary(player1, List.of(topCard, below));
        harness.enterBattlefieldAndReturn(player1, enteringPermanent);
        harness.passBothPriorities();
    }
}
