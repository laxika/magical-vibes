package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntroductionToAnnihilation;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BiblioplexAssistant.class, GrizzlyBears.class, Shock.class, IntroductionToAnnihilation.class})
class BiblioplexAssistantTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a targeted instant or sorcery from the graveyard on top of the library")
    void etbPutsTargetedInstantOrSorceryOnTopOfLibrary() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears(), shock)));
        harness.setLibrary(player1, new ArrayList<>());

        castAssistant();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(shock.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Shock");
        harness.assertNotInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The up-to-one ETB may choose no card")
    void etbMayChooseNoCard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));
        harness.setLibrary(player1, new ArrayList<>());

        castAssistant();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("ETB does not target a creature card")
    void etbDoesNotTargetCreatureCard() {
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>());

        castAssistant();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB returns one sorcery above the existing library and excludes opposing graveyards")
    void returnsSorceryAboveExistingLibrary() {
        IntroductionToAnnihilation chosen = new IntroductionToAnnihilation();
        IntroductionToAnnihilation other = new IntroductionToAnnihilation();
        IntroductionToAnnihilation opposing = new IntroductionToAnnihilation();
        BiblioplexAssistant libraryCard = new BiblioplexAssistant();
        harness.setGraveyard(player1, new ArrayList<>(List.of(chosen, other)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opposing)));
        harness.setLibrary(player1, List.of(libraryCard));

        castAssistant();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), other.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen, libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposing);
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard")
    void doesNotReplaceTargetThatLeftGraveyard() {
        IntroductionToAnnihilation chosen = new IntroductionToAnnihilation();
        IntroductionToAnnihilation other = new IntroductionToAnnihilation();
        BiblioplexAssistant libraryCard = new BiblioplexAssistant();
        harness.setGraveyard(player1, new ArrayList<>(List.of(chosen, other)));
        harness.setLibrary(player1, List.of(libraryCard));

        castAssistant();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.setGraveyard(player1, new ArrayList<>(List.of(other)));
        harness.setHand(player1, List.of(chosen));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        harness.assertInHand(player1, "Introduction to Annihilation");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    private void castAssistant() {
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new BiblioplexAssistant(), "{4}");
        harness.passBothPriorities();
    }
}
