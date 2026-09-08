package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
import com.github.laxika.magicalvibes.cards.k.KokushoTheEveningStar;
import com.github.laxika.magicalvibes.cards.s.SqueeTheImmortal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurtlesForever.class, ArvadTheCursed.class, GrizzlyBears.class,
        KamahlPitFighter.class, KokushoTheEveningStar.class, SqueeTheImmortal.class})
class TurtlesForeverTest extends BaseCardTest {

    @Test
    @DisplayName("Searches the library and sideboard, then puts the opponent's two choices into hand")
    void searchesLibraryAndSideboard() {
        Card arvad = new ArvadTheCursed();
        Card kamahl = new KamahlPitFighter();
        Card kokusho = new KokushoTheEveningStar();
        Card squee = new SqueeTheImmortal();
        Card ineligible = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ineligible, arvad, kamahl, kokusho));
        setSideboard(squee);
        castSpell();

        PendingInteraction.TurtlesForeverSearchChoice search =
                gd.interaction.activeInteraction(PendingInteraction.TurtlesForeverSearchChoice.class);
        assertThat(search).isNotNull();
        assertThat(search.pool()).containsExactly(arvad, kamahl, kokusho, squee);

        harness.handleMultipleCardsChosen(player1, search.validCardIds());
        PendingInteraction.TurtlesForeverOpponentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.TurtlesForeverOpponentChoice.class);
        assertThat(opponentChoice).isNotNull();
        assertThat(opponentChoice.playerId()).isEqualTo(player2.getId());

        harness.handleMultipleCardsChosen(player2, List.of(arvad.getId(), squee.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(arvad, squee);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(ineligible, kamahl, kokusho);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Turtles Forever");
    }

    @Test
    @DisplayName("Cannot find four differently named legendary creatures when fewer are available")
    void fewerThanFourDifferentNamesFindsNothing() {
        Card arvad = new ArvadTheCursed();
        Card duplicateArvad = new ArvadTheCursed();
        Card kokusho = new KokushoTheEveningStar();
        Card ineligible = new GrizzlyBears();
        harness.setLibrary(player1, List.of(arvad, duplicateArvad, kokusho, ineligible));
        castSpell();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(arvad, duplicateArvad, kokusho, ineligible);
        harness.assertInGraveyard(player1, "Turtles Forever");
    }

    @Test
    @DisplayName("Requires four different names and then exactly two opponent choices")
    void validatesBothSelections() {
        Card arvad = new ArvadTheCursed();
        Card duplicateArvad = new ArvadTheCursed();
        Card kamahl = new KamahlPitFighter();
        Card kokusho = new KokushoTheEveningStar();
        Card squee = new SqueeTheImmortal();
        harness.setLibrary(player1, List.of(arvad, duplicateArvad, kamahl, kokusho, squee));
        castSpell();

        PendingInteraction.TurtlesForeverSearchChoice search =
                gd.interaction.activeInteraction(PendingInteraction.TurtlesForeverSearchChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(arvad.getId(), duplicateArvad.getId(), kamahl.getId(), kokusho.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different names");
        assertThat(gd.interaction.activeInteraction()).isSameAs(search);

        harness.handleMultipleCardsChosen(player1,
                List.of(arvad.getId(), kamahl.getId(), kokusho.getId(), squee.getId()));
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(arvad.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 2");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TurtlesForeverOpponentChoice.class))
                .isNotNull();
    }

    private void castSpell() {
        harness.setHand(player1, List.of(new TurtlesForever()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void setSideboard(Card... cards) {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(cards)));
    }
}
