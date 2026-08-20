package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IntoTheNorthTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving offers only snow lands for the tapped battlefield search")
    void resolvingOffersOnlySnowLands() {
        SnowCoveredForest snowForest = new SnowCoveredForest();
        SnowCoveredIsland snowIsland = new SnowCoveredIsland();
        setupAndCast(List.of(snowForest, new Forest(), snowIsland, new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(snowForest, snowIsland);
    }

    @Test
    @DisplayName("Choosing a snow land puts it onto the battlefield tapped")
    void chosenSnowLandEntersTapped() {
        SnowCoveredForest snowForest = new SnowCoveredForest();
        setupAndCast(List.of(snowForest));

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == snowForest && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may fail to find a snow land")
    void canFailToFind() {
        setupAndCast(List.of(new Forest(), new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void setupAndCast(List<Card> library) {
        harness.setHand(player1, List.of(new IntoTheNorth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, library);
        harness.castSorcery(player1, 0, 0);
    }
}
