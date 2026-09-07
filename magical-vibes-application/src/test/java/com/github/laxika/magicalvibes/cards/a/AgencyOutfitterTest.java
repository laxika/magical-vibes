package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgencyOutfitter.class, MagnifyingGlass.class})
class AgencyOutfitterTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may ability puts both named cards from the graveyard and hand onto the battlefield")
    void findsNamedCardsInGraveyardAndHand() {
        harness.setGraveyard(player1, List.of(new MagnifyingGlass()));
        harness.setHand(player1, List.of(new AgencyOutfitter(), thinkingCap()));
        castAgencyOutfitter();

        resolveEnterTriggerAndAcceptSearch();

        harness.assertOnBattlefield(player1, "Magnifying Glass");
        harness.assertOnBattlefield(player1, "Thinking Cap");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting the may ability searches the library for either named card")
    void findsNamedCardsInLibrary() {
        harness.setLibrary(player1, List.of(new MagnifyingGlass(), thinkingCap()));
        harness.setHand(player1, List.of(new AgencyOutfitter()));
        castAgencyOutfitter();

        resolveEnterTriggerAndAcceptSearch();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanents(player1, "Magnifying Glass")).hasSize(1);
        assertThat(findPermanents(player1, "Thinking Cap")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may ability does not search")
    void decliningSearchDoesNothing() {
        harness.setGraveyard(player1, List.of(new MagnifyingGlass()));
        harness.setHand(player1, List.of(new AgencyOutfitter(), thinkingCap()));
        castAgencyOutfitter();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Magnifying Glass")).isEmpty();
        assertThat(findPermanents(player1, "Thinking Cap")).isEmpty();
        harness.assertInGraveyard(player1, "Magnifying Glass");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Thinking Cap");
    }

    private void castAgencyOutfitter() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void resolveEnterTriggerAndAcceptSearch() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private Card thinkingCap() {
        Card card = new Card();
        card.setName("Thinking Cap");
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{1}");
        return card;
    }
}
