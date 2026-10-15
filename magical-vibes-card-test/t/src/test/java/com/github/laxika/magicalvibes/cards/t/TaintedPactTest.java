package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.e.Embolden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaintedPact.class, DuskImp.class, Embolden.class})
class TaintedPactTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the first unique exiled card into hand and stops")
    void putsAcceptedCardIntoHandAndStops() {
        Card first = new DuskImp();
        Card second = new Embolden();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        harness.assertInGraveyard(player1, "Tainted Pact");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Continues exiling when a unique card is declined")
    void continuesAfterDecliningUniqueCard() {
        Card first = new DuskImp();
        Card second = new Embolden();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Tainted Pact");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).containsExactly("Dusk Imp");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Stops after declining the last unique card")
    void stopsAfterDecliningLastUniqueCard() {
        Card only = new DuskImp();
        harness.setLibrary(player1, List.of(only));
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Tainted Pact");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).containsExactly("Dusk Imp");
    }

    @Test
    @DisplayName("Stops on a duplicate name without offering it to hand")
    void stopsOnDuplicateName() {
        Card first = new DuskImp();
        Card duplicate = new DuskImp();
        Card remaining = new Embolden();
        harness.setLibrary(player1, List.of(first, duplicate, remaining));
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, duplicate);
        harness.assertInGraveyard(player1, "Tainted Pact");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Dusk Imp", "Dusk Imp");
    }

    @Test
    @DisplayName("Stops without prompting when the library is empty")
    void stopsWhenLibraryIsEmpty() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Tainted Pact");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("A matching card already in exile does not prevent accepting a card")
    void ignoresCardsExiledBeforeThisResolution() {
        Card previouslyExiled = new DuskImp();
        Card first = new DuskImp();
        Card remaining = new Embolden();
        harness.setExile(player1, List.of(previouslyExiled));
        harness.setLibrary(player1, List.of(first, remaining));
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(previouslyExiled);
        harness.assertInGraveyard(player1, "Tainted Pact");
    }

    @Test
    @DisplayName("A duplicate of an earlier nonadjacent card stops the process")
    void stopsOnNonadjacentDuplicateName() {
        Card first = new DuskImp();
        Card second = new Embolden();
        Card duplicate = new DuskImp();
        Card remaining = new Embolden();
        harness.setLibrary(player1, List.of(first, second, duplicate, remaining));
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(first, second, duplicate);
        harness.assertInGraveyard(player1, "Tainted Pact");
    }

    @Test
    @DisplayName("Only the current card can be accepted after several declines")
    void acceptsOnlyCurrentCardAfterSeveralDeclines() {
        Card first = new DuskImp();
        Card second = new Embolden();
        Card third = new TaintedPact();
        Card remaining = new DuskImp();
        harness.setLibrary(player1, List.of(first, second, third, remaining));
        harness.castFromHand(player1, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Tainted Pact");
    }

    @Test
    @DisplayName("Uses the controller's library and hand when the other player casts it")
    void usesOtherPlayersLibraryAndHand() {
        Card untouched = new DuskImp();
        Card first = new Embolden();
        Card remaining = new DuskImp();
        harness.setLibrary(player1, List.of(untouched));
        harness.setLibrary(player2, List.of(first, remaining));
        harness.castFromHand(player2, new TaintedPact(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player2, "Tainted Pact");
    }
}
