package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronrootTreefolk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TendTheSprigsTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land and puts it onto the battlefield tapped")
    void searchesForBasicLandTapped() {
        setupLibrary(new Forest(), new GrizzlyBears());
        castTendTheSprigs();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).singleElement().satisfies(card ->
                assertThat(card.hasType(CardType.LAND)).isTrue());

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent.isTapped());
    }

    @Test
    @DisplayName("Creates a Treefolk when the fetched land reaches seven matching permanents")
    void createsTreefolkAfterFetchingSeventhLand() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        setupLibrary(new Forest());
        castTendTheSprigs();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREEFOLK)
                        && permanent.getEffectivePower() == 3
                        && permanent.getEffectiveToughness() == 4
                        && permanent.hasKeyword(Keyword.REACH));
    }

    @Test
    @DisplayName("Does not create a Treefolk with fewer than seven lands and Treefolk")
    void doesNotCreateTreefolkBelowThreshold() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        setupLibrary(new Forest());
        castTendTheSprigs();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREEFOLK));
    }

    @Test
    @DisplayName("Counts controlled Treefolk toward the threshold")
    void countsTreefolkTowardThreshold() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.addToBattlefield(player1, new IronrootTreefolk());
        setupLibrary(new Forest());
        castTendTheSprigs();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.hasKeyword(Keyword.REACH))
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getEffectivePower()).isEqualTo(3);
                    assertThat(permanent.getEffectiveToughness()).isEqualTo(4);
                });
    }

    private void castTendTheSprigs() {
        harness.setHand(player1, List.of(new TendTheSprigs()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
    }

    private void setupLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
