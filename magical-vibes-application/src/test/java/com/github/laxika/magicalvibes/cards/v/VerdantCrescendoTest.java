package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NissaNaturesArtisan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerdantCrescendo.class, Forest.class, NissaNaturesArtisan.class})
class VerdantCrescendoTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a basic land onto the battlefield tapped, then finds Nissa from the library")
    void searchesBasicLandAndNissaFromLibrary() {
        Card forest = new Forest();
        Card nissa = new NissaNaturesArtisan();
        harness.setLibrary(player1, List.of(forest, nissa));
        castVerdantCrescendo();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch basicLandSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(basicLandSearch).isNotNull();
        assertThat(basicLandSearch.params().cards()).containsExactly(forest);
        assertThat(basicLandSearch.params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch nissaSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(nissaSearch).isNotNull();
        assertThat(nissaSearch.params().filterCardName()).isEqualTo("Nissa, Nature's Artisan");
        assertThat(nissaSearch.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(forest);
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerHands.get(player1.getId())).contains(nissa);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Takes Nissa from the graveyard when the library has no basic land")
    void searchesGraveyardForNissaWhenBasicLandIsUnavailable() {
        Card nissa = new NissaNaturesArtisan();
        harness.setGraveyard(player1, List.of(nissa));
        harness.setLibrary(player1, List.of());
        castVerdantCrescendo();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(nissa);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nissa);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castVerdantCrescendo() {
        harness.castFromHand(player1, new VerdantCrescendo(), "{3}{G}");
    }
}
