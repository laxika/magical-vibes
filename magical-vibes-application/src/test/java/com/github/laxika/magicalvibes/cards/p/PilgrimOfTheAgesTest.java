package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlacialFortress;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PilgrimOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB ability may search for a basic Plains card")
    void etbSearchesForBasicPlains() {
        harness.setHand(player1, List.of(new PilgrimOfTheAges()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        Card plains = new Plains();
        Card nonBasicPlains = new GlacialFortress();
        Card forest = new Forest();
        setLibrary(plains, nonBasicPlains, forest);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonBasicPlains, forest);
    }

    @Test
    @DisplayName("The graveyard ability returns Pilgrim of the Ages to its owner's hand")
    void graveyardAbilityReturnsItToHand() {
        Card pilgrim = new PilgrimOfTheAges();
        harness.setGraveyard(player1, List.of(pilgrim));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(pilgrim);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pilgrim);
    }

    @Test
    @DisplayName("Declining the ETB ability does not search")
    void decliningEtbLeavesLibraryUnchanged() {
        harness.setHand(player1, List.of(new PilgrimOfTheAges()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        Card plains = new Plains();
        setLibrary(plains);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(plains);
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
