package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.o.OchreJelly;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TashasHideousLaughter.class, Forest.class, GrizzlyBears.class, HillGiantHerdgorger.class, OchreJelly.class})
class TashasHideousLaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent exiles from their library until total mana value 20")
    void exilesUntilTotalManaValueTwenty() {
        Card controllerLibraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(controllerLibraryCard));

        List<Card> opponentLibrary = new ArrayList<>();
        opponentLibrary.add(new Forest());
        for (int i = 0; i < 10; i++) {
            opponentLibrary.add(new GrizzlyBears());
        }
        Card remainingCard = new Forest();
        opponentLibrary.add(remainingCard);
        harness.setLibrary(player2, opponentLibrary);

        harness.setHand(player1, List.of(new TashasHideousLaughter()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(opponentLibrary.subList(0, 11).stream()
                        .map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibraryCard);
        assertThat(gd.exilePlayPermissions).isEmpty();
        assertThat(gd.exilePlayWithoutPayingManaCost).isEmpty();
    }

    @Test
    @DisplayName("Exiles the whole library when it cannot reach total mana value 20")
    void exilesWholeLibraryWhenThresholdCannotBeReached() {
        Card land = new Forest();
        Card spell = new GrizzlyBears();
        harness.setLibrary(player2, List.of(land, spell));
        harness.setHand(player1, List.of(new TashasHideousLaughter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land, spell);
    }

    @Test
    @DisplayName("Includes the card that takes total mana value above twenty")
    void exilesCardThatExceedsThreshold() {
        List<Card> exiledCards = List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger(),
                new HillGiantHerdgorger(), new HillGiantHerdgorger());
        Card remainingCard = new Forest();
        List<Card> library = new ArrayList<>(exiledCards);
        library.add(remainingCard);
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new TashasHideousLaughter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyElementsOf(exiledCards);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("X contributes zero to the mana value of cards exiled from the library")
    void countsXAsZeroOutsideStack() {
        List<Card> exiledCards = List.of(new HillGiantHerdgorger(), new HillGiantHerdgorger(),
                new HillGiantHerdgorger(), new OchreJelly(), new OchreJelly());
        Card remainingCard = new Forest();
        List<Card> library = new ArrayList<>(exiledCards);
        library.add(remainingCard);
        harness.setLibrary(player2, library);
        harness.setHand(player1, List.of(new TashasHideousLaughter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyElementsOf(exiledCards);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("An empty opponent library does not prevent the spell from resolving")
    void resolvesWithEmptyOpponentLibrary() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new TashasHideousLaughter()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Tasha's Hideous Laughter");
        assertThat(gd.stack).isEmpty();
    }
}
