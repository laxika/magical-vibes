package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExplosiveVegetation.class, Forest.class, Plains.class, GrizzlyBears.class})
class ExplosiveVegetationTest extends BaseCardTest {

    @Test
    @DisplayName("Offers up to two basic lands for the battlefield tapped")
    void offersBasicLandsForBattlefieldTapped() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        castWithLibrary(List.of(forest, plains, new GrizzlyBears()));

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest, plains);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May find only one basic land")
    void mayFindOnlyOneBasicLand() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        castWithLibrary(List.of(forest, bears));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allMatch(permanent -> permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May fail to find a basic land")
    void mayFailToFind() {
        castWithLibrary(List.of(new Forest(), new GrizzlyBears()));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castWithLibrary(List<Card> library) {
        harness.setHand(player1, List.of(new ExplosiveVegetation()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
