package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToralfsDisciple.class, LightningBolt.class})
class ToralfsDiscipleTest extends BaseCardTest {

    @Test
    void attackingConjuresFourLightningBoltsIntoTheLibraryAndShuffles() {
        addCreatureReady(player1, new ToralfsDisciple());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(4)
                .extracting(Card::getName)
                .containsOnly("Lightning Bolt");
        assertThat(gameLogContains("conjures 4 cards named Lightning Bolt into"))
                .isTrue();
    }

    @Test
    void conjuringPreservesExistingCardsAndCreatesDistinctOwnedCards() {
        addCreatureReady(player1, new ToralfsDisciple());
        Card existingCard = new ToralfsDisciple();
        harness.setLibrary(player1, List.of(existingCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(5).contains(existingCard);
        List<Card> bolts = library.stream()
                .filter(card -> card.getName().equals("Lightning Bolt"))
                .toList();
        assertThat(bolts).hasSize(4);
        assertThat(bolts.stream().map(Card::getId).distinct().count()).isEqualTo(4);
        assertThat(bolts).allSatisfy(card ->
                assertThat(card.getOwnerId()).isEqualTo(player1.getId()));
    }

    @Test
    void otherPlayersDiscipleConjuresIntoItsControllersLibrary() {
        addCreatureReady(player2, new ToralfsDisciple());
        Card untouchedCard = new ToralfsDisciple();
        harness.setLibrary(player1, List.of(untouchedCard));
        harness.setLibrary(player2, List.of());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouchedCard);
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(4)
                .allSatisfy(card -> {
                    assertThat(card.getName()).isEqualTo("Lightning Bolt");
                    assertThat(card.getOwnerId()).isEqualTo(player2.getId());
                });
    }

    @Test
    void eachAttackingDiscipleConjuresFourCards() {
        addCreatureReady(player1, new ToralfsDisciple());
        addCreatureReady(player1, new ToralfsDisciple());
        addCreatureReady(player1, new ToralfsDisciple());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(8)
                .extracting(Card::getName)
                .containsOnly("Lightning Bolt");
    }
}
