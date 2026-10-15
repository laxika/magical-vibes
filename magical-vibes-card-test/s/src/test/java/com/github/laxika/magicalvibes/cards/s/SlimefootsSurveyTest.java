package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TangledIslet;
import com.github.laxika.magicalvibes.cards.y.YavimayaSojourner;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlimefootsSurvey.class, Forest.class, TangledIslet.class, YavimayaSojourner.class})
class SlimefootsSurveyTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for lands with basic land types, then looks at the domain number of cards")
    void searchesAndSelectsFromDynamicNumberOfTopCards() {
        Card forest = new Forest();
        Card islet = new TangledIslet();
        Card creature = new YavimayaSojourner();
        Card spell = new SlimefootsSurvey();
        Card otherCreature = new YavimayaSojourner();
        harness.setLibrary(player1, List.of(forest, islet, creature, spell, otherCreature));

        castSurvey();

        PendingInteraction.LibrarySearch search = activeLibrarySearch();
        assertThat(search.params().cards()).containsExactly(forest, islet);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);
        activeLibrarySearch();
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch topCards = activeLibrarySearch();
        assertThat(topCards.params().sourceCards()).hasSize(2);
        Card chosen = topCards.params().sourceCards().getFirst();
        Card unchosen = topCards.params().sourceCards().get(1);
        Card unlooked = gd.playerDecks.get(player1.getId()).getFirst();
        int chosenIndex = topCards.params().cards().indexOf(chosen);
        harness.handleCardChosen(player1, chosenIndex);

        assertThat(findPermanent(player1, forest.getName()).isTapped()).isTrue();
        assertThat(findPermanent(player1, islet.getName()).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen, unlooked, unchosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, spell, otherCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can decline the optional top-card choice")
    void optionalTopCardChoiceCanBeDeclined() {
        Card forest = new Forest();
        Card islet = new TangledIslet();
        Card creature = new YavimayaSojourner();
        Card spell = new SlimefootsSurvey();
        harness.setLibrary(player1, List.of(forest, islet, creature, spell));

        castSurvey();

        harness.handleCardChosen(player1, 0);
        activeLibrarySearch();
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch topCards = activeLibrarySearch();
        assertThat(topCards.params().sourceCards()).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest)
                .anyMatch(permanent -> permanent.getCard() == islet);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The selected top card remains private")
    void doesNotRevealChosenTopCard() {
        Card creature = new YavimayaSojourner();
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(creature));

        castSurvey();
        activeLibrarySearch();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.gameLog).noneMatch(entry -> entry.plainText().contains("reveals " + creature.getName()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Both searched lands enter together after selection is complete")
    void searchedLandsDoNotEnterDuringSelection() {
        Card forest = new Forest();
        Card islet = new TangledIslet();
        harness.setLibrary(player1, List.of(forest, islet));

        castSurvey();
        harness.handleCardChosen(player1, 0);
        activeLibrarySearch();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, forest.getName()).isTapped()).isTrue();
        assertThat(findPermanent(player1, islet.getName()).isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can find one land and decline the second while still looking at cards")
    void canFindOnlyOneLand() {
        Card forest = new Forest();
        Card islet = new TangledIslet();
        Card creature = new YavimayaSojourner();
        harness.setLibrary(player1, List.of(forest, islet, creature));

        castSurvey();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(activeLibrarySearch().params().sourceCards()).hasSize(1);
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanent(player1, forest.getName()).isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, islet.getName());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(islet, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Finding no lands with zero domain finishes without a top-card choice")
    void canFindNoLandsWithZeroDomain() {
        Card forest = new Forest();
        Card creature = new YavimayaSojourner();
        harness.addToBattlefield(player2, new TangledIslet());
        harness.setLibrary(player1, List.of(forest, creature));

        castSurvey();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, creature);
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
}
