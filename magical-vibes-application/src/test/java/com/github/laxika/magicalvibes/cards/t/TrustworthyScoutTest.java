package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TrustworthyScout.class)
class TrustworthyScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability exiles the source and searches for another Trustworthy Scout")
    void exilesSourceAndSearchesForScout() {
        TrustworthyScout source = new TrustworthyScout();
        TrustworthyScout found = new TrustworthyScout();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(source));
        harness.setLibrary(player1, List.of(found));
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(source.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId).contains(source.getId());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getId).containsExactly(found.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals())
                .isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(found.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ability finds nothing when no Trustworthy Scout is in the library")
    void findsNothingWithoutScoutInLibrary() {
        TrustworthyScout source = new TrustworthyScout();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(source));
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId).contains(source.getId());
    }
}
