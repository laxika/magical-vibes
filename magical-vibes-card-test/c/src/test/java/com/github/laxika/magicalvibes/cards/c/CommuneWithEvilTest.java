package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommuneWithEvil.class, GrizzlyBears.class, Plains.class, Shock.class})
class CommuneWithEvilTest extends BaseCardTest {

    @Test
    void putsOneCardIntoHandAndTheRestIntoGraveyardThenGainsLife() {
        Card first = new Shock();
        Card chosen = new GrizzlyBears();
        Card third = new Plains();
        Card fourth = new Shock();
        harness.setLibrary(player1, List.of(first, chosen, third, fourth));

        harness.castFromHand(player1, new CommuneWithEvil(), "{2}{B}");
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void gainsLifeWhenTheLibraryIsEmpty() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new CommuneWithEvil(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void putsTheOnlyLibraryCardIntoHandAndGainsLife() {
        Card onlyCard = new Plains();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.castFromHand(player1, new CommuneWithEvil(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(onlyCard.getId()));
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    void choosesOneFromAShortLibraryAndPutsTheOtherIntoGraveyard() {
        Card chosen = new Plains();
        Card other = new CommuneWithEvil();
        harness.setLibrary(player1, List.of(chosen, other));

        harness.castFromHand(player1, new CommuneWithEvil(), "{2}{B}");
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 23);
    }

    @Test
    void requiresOneCardAndLeavesCardsBelowTheTopFourInOrder() {
        Card first = new Plains();
        Card second = new CommuneWithEvil();
        Card third = new Plains();
        Card chosen = new Plains();
        Card fifth = new CommuneWithEvil();
        Card sixth = new Plains();
        harness.setLibrary(player1, List.of(first, second, third, chosen, fifth, sixth));

        harness.castFromHand(player1, new CommuneWithEvil(), "{2}{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId(), chosen.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(fifth.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third)
                .doesNotContain(chosen, fifth, sixth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, sixth);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }
}
