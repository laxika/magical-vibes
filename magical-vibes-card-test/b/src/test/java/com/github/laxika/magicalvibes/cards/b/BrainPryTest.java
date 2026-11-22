package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CoilingOracle;
import com.github.laxika.magicalvibes.cards.l.LibraryOfLeng;
import com.github.laxika.magicalvibes.cards.s.StompAndHowl;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrainPry.class, BloodCrypt.class, CoilingOracle.class, StompAndHowl.class,
        TamiyoCollectorOfTales.class, LibraryOfLeng.class})
class BrainPryTest extends BaseCardTest {

    @Test
    @DisplayName("The target chooses one matching card to discard")
    void targetChoosesOneMatchingCard() {
        Card firstStompAndHowl = new StompAndHowl();
        Card secondStompAndHowl = new StompAndHowl();
        Card coilingOracle = new CoilingOracle();
        cast(List.of(firstStompAndHowl, secondStompAndHowl, coilingOracle));

        harness.handleListChoice(player1, "Stomp and Howl");

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(choice.cards()).containsExactly(firstStompAndHowl, secondStompAndHowl);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(coilingOracle.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(firstStompAndHowl.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player2, List.of(secondStompAndHowl.getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstStompAndHowl, coilingOracle);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondStompAndHowl);
    }

    @Test
    @DisplayName("Draws a card when the named card is absent from the target's hand")
    void drawsWhenNamedCardIsAbsent() {
        CoilingOracle drawnCard = new CoilingOracle();
        harness.setLibrary(player1, List.of(drawnCard));
        cast(List.of(new StompAndHowl()));

        harness.handleListChoice(player1, "Coiling Oracle");

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Stomp and Howl");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Only offers nonland card names")
    void onlyOffersNonlandCardNames() {
        cast(List.of(new BloodCrypt(), new StompAndHowl()));

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("Stomp and Howl").doesNotContain("Blood Crypt");
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetController() {
        BrainPry brainPry = new BrainPry();
        Card firstStompAndHowl = new StompAndHowl();
        Card secondStompAndHowl = new StompAndHowl();
        harness.setHand(player1, List.of(brainPry, firstStompAndHowl, secondStompAndHowl));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.handleListChoice(player1, "Stomp and Howl");
        harness.handleMultipleCardsChosen(player1, List.of(secondStompAndHowl.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstStompAndHowl);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(brainPry, secondStompAndHowl);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void rejectsPermanentTarget() {
        BrainPry brainPry = new BrainPry();
        harness.setHand(player1, List.of(brainPry));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoilingOracle());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(brainPry);
    }

    @Test
    @DisplayName("Draws exactly one card when the target has an empty hand")
    void drawsWhenTargetHandIsEmpty() {
        Card drawnCard = new CoilingOracle();
        Card nextCard = new StompAndHowl();
        harness.setLibrary(player1, List.of(drawnCard, nextCard));
        cast(List.of());

        harness.handleListChoice(player1, "Coiling Oracle");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A successful discard is mandatory and does not draw a card")
    void successfulDiscardDoesNotDraw() {
        Card matchingCard = new StompAndHowl();
        Card otherCard = new CoilingOracle();
        Card libraryCard = new CoilingOracle();
        harness.setLibrary(player1, List.of(libraryCard));
        cast(List.of(matchingCard, otherCard));

        harness.handleListChoice(player1, "Stomp and Howl");

        // The only card with that name is discarded: nothing is left to choose.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(otherCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(matchingCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Rejects naming a land without consuming the name choice")
    void rejectsLandName() {
        Card matchingCard = new StompAndHowl();
        cast(List.of(new BloodCrypt(), matchingCard));

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Blood Crypt"))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, "Stomp and Howl");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(matchingCard);
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Blood Crypt");
    }

    @Test
    @DisplayName("Draws a card when the target can't be made to discard the named card")
    void drawsWhenDiscardIsPrevented() {
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player2, new TamiyoCollectorOfTales());
        tamiyo.setCounterCount(CounterType.LOYALTY, 5);
        Card drawnCard = new CoilingOracle();
        Card matchingCard = new StompAndHowl();
        harness.setLibrary(player1, List.of(drawnCard));
        cast(List.of(matchingCard));

        harness.handleListChoice(player1, "Stomp and Howl");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(matchingCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("A discard put on top of the library instead counts as discarded, so the caster doesn't draw")
    void discardToLibraryIsADiscard() {
        harness.addToBattlefield(player2, new LibraryOfLeng());
        Card matchingCard = new StompAndHowl();
        Card notDrawn = new CoilingOracle();
        harness.setLibrary(player1, List.of(notDrawn));
        cast(List.of(matchingCard));

        harness.handleListChoice(player1, "Stomp and Howl");
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(matchingCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(notDrawn);
    }

    @Test
    @DisplayName("Declining the replacement discards to the graveyard, and the caster doesn't draw")
    void declinedReplacementIsADiscard() {
        harness.addToBattlefield(player2, new LibraryOfLeng());
        Card matchingCard = new StompAndHowl();
        Card notDrawn = new CoilingOracle();
        harness.setLibrary(player1, List.of(notDrawn));
        cast(List.of(matchingCard));

        harness.handleListChoice(player1, "Stomp and Howl");
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(matchingCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(notDrawn);
    }

    private void cast(List<Card> targetHand) {
        harness.setHand(player1, List.of(new BrainPry()));
        harness.setHand(player2, targetHand);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
    }
}
