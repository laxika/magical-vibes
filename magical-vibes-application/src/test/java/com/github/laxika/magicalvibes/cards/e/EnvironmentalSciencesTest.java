package com.github.laxika.magicalvibes.cards.e;

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

class EnvironmentalSciencesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only basic lands to put into hand")
    void resolvesOffersBasicLandsToHand() {
        castEnvironmentalSciences();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(3);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Putting a basic land into hand also gives two life")
    void putsBasicLandIntoHandAndGainsLife() {
        castEnvironmentalSciences();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        harness.assertInGraveyard(player1, "Environmental Sciences");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castEnvironmentalSciences() {
        harness.setHand(player1, List.of(new EnvironmentalSciences()));
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
