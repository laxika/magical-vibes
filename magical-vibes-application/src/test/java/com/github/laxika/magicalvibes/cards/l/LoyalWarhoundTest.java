package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalWarhound.class, Forest.class, GrizzlyBears.class, Plains.class})
class LoyalWarhoundTest extends BaseCardTest {

    @Test
    @DisplayName("ETB searches for a basic Plains when an opponent controls more lands")
    void searchesForBasicPlainsWhenOpponentHasMoreLands() {
        castWarhound();
        harness.addToBattlefield(player2, new Forest());
        setupLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Plains"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent plains = findPermanent(player1, "Plains");
        assertThat(plains.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not search when no opponent controls more lands")
    void doesNotSearchWhenOpponentDoesNotHaveMoreLands() {
        castWarhound();
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The intervening-if condition is checked again when the ETB resolves")
    void doesNotSearchIfLandCountsEqualizeBeforeResolution() {
        castWarhound();
        harness.addToBattlefield(player2, new Forest());
        setupLibrary();

        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    private void castWarhound() {
        harness.setHand(player1, List.of(new LoyalWarhound()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new GrizzlyBears()));
    }
}
