package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmarethTheLustrous.class, GrizzlyBears.class, Shock.class, HowlingMine.class, MarchOfTheMachines.class})
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

    @Test
    @DisplayName("An empty library offers no card and does not cause a failed draw")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new AmarethTheLustrous());
        harness.setLibrary(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("An animated artifact shares its creature type with a creature card")
    void animatedArtifactMatchesCreatureCard() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        enterAnotherPermanentWithLibrary(new HowlingMine(), topCard, new Shock());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("An animated artifact that dies retains its last battlefield types for Amareth")
    void departedAnimatedArtifactUsesLastKnownCreatureType() {
        Card topCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new AmarethTheLustrous());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.setLibrary(player1, List.of(topCard));
        Permanent mine = harness.enterBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, mine.getId());
        harness.assertInGraveyard(player1, "Howling Mine");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }
    @Test
    @DisplayName("Looking at a nonmatching card privately shows it to the controller")
    void nonmatchingTopCardIsShownPrivately() {
        harness.clearMessages();
        enterAnotherPermanentWithLibrary(new GrizzlyBears(), new Shock(), new GrizzlyBears());

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP"))
                .anyMatch(message -> message.contains("Shock"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
    }
    private void enterAnotherPermanentWithLibrary(Card enteringPermanent, Card topCard, Card below) {
        harness.addToBattlefield(player1, new AmarethTheLustrous());
        harness.setLibrary(player1, List.of(topCard, below));
        harness.enterBattlefieldAndReturn(player1, enteringPermanent);
        harness.passBothPriorities();
    }
}
