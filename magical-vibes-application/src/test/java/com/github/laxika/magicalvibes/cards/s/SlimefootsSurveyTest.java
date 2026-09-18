package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZagothTriome;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({SlimefootsSurvey.class, Forest.class, ZagothTriome.class, GrizzlyBears.class, Shock.class})
class SlimefootsSurveyTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for lands with basic land types, then looks at the domain number of cards")
    void searchesAndSelectsFromDynamicNumberOfTopCards() {
        Card forest = new Forest();
        Card triome = new ZagothTriome();
        Card creature = new GrizzlyBears();
        Card spell = new Shock();
        Card otherCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, triome, creature, spell, otherCreature));

        castSurvey();

        PendingInteraction.LibrarySearch search = activeLibrarySearch();
        assertThat(search.params().cards()).containsExactly(forest, triome);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        search = activeLibrarySearch();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        PendingInteraction.LibrarySearch topCards = activeLibrarySearch();
        assertThat(topCards.params().sourceCards()).hasSize(3);
        Card chosen = topCards.params().sourceCards().getFirst();
        int chosenIndex = topCards.params().cards().indexOf(chosen);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(chosenIndex));

        assertThat(permanentFor(forest).isTapped()).isTrue();
        assertThat(permanentFor(triome).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, spell, otherCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can decline the optional top-card choice")
    void optionalTopCardChoiceCanBeDeclined() {
        Card forest = new Forest();
        Card triome = new ZagothTriome();
        Card creature = new GrizzlyBears();
        Card spell = new Shock();
        harness.setLibrary(player1, List.of(forest, triome, creature, spell));

        castSurvey();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        activeLibrarySearch();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        PendingInteraction.LibrarySearch topCards = activeLibrarySearch();
        assertThat(topCards.params().sourceCards()).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest)
                .anyMatch(permanent -> permanent.getCard() == triome);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castSurvey() {
        harness.setHand(player1, List.of(new SlimefootsSurvey()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private PendingInteraction.LibrarySearch activeLibrarySearch() {
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        return search;
    }

    private com.github.laxika.magicalvibes.model.Permanent permanentFor(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }
}
