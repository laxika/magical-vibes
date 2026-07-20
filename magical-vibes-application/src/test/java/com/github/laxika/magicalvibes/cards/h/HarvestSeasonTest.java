package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HarvestSeasonTest extends BaseCardTest {

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }

    private void castHarvestSeason() {
        harness.setHand(player1, List.of(new HarvestSeason()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("X equals the number of tapped creatures you control and only basic lands are offered, to the battlefield tapped")
    void searchCountEqualsTappedCreatures() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).tap();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).tap();
        setupLibrary();
        castHarvestSeason();

        harness.passBothPriorities();

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().remainingCount()).isEqualTo(2);
        assertThat(activeSearch().params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(activeSearch().params().cards())
                .allMatch(c -> c.hasType(CardType.LAND) && c.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Chosen basic lands enter the battlefield tapped")
    void chosenLandsEnterTapped() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).tap();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).tap();
        setupLibrary();
        castHarvestSeason();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(com.github.laxika.magicalvibes.model.Permanent::isTapped);
    }

    @Test
    @DisplayName("With no tapped creatures X is zero, so no lands enter the battlefield")
    void noTappedCreaturesFindsNoLands() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        setupLibrary();
        castHarvestSeason();

        harness.passBothPriorities();
        // X is 0 (the only creature is untapped), so the degenerate "up to 0" search finds nothing.
        assertThat(activeSearch().params().remainingCount()).isZero();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(activeSearch()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
    }
}
