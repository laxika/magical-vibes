package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GloryheathLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled searches for a basic Plains")
    void attacksWhileSaddledSearchesForBasicPlains() {
        Permanent lynx = addCreatureReady(player1, new GloryheathLynx());
        setLibrary(new Plains(), new Forest());
        lynx.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Plains")
                        && card.getSupertypes().contains(CardSupertype.BASIC));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Attacking while not saddled does not search")
    void doesNotSearchWhenNotSaddled() {
        addCreatureReady(player1, new GloryheathLynx());
        setLibrary(new Plains());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Plains"));
    }

    @Test
    @DisplayName("The trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent lynx = addCreatureReady(player1, new GloryheathLynx());
        setLibrary(new Plains());

        declareAttackers(player1, List.of(0));
        lynx.setSaddled(true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
