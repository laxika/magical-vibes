package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BindingTheOldGods;
import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.h.HalvarGodOfBattle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearchForGlory.class, SnowCoveredPlains.class, HalvarGodOfBattle.class,
        BindingTheOldGods.class, FearlessPup.class})
class SearchForGloryTest extends BaseCardTest {

    @Test
    void searchesForSnowPermanentsLegendariesAndSagas() {
        harness.setHand(player1, List.of(new SearchForGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.setLibrary(player1, List.of(
                new SnowCoveredPlains(), new HalvarGodOfBattle(), new BindingTheOldGods(),
                new FearlessPup(), new SearchForGlory()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Snow-Covered Plains", "Halvar, God of Battle", "Binding the Old Gods");

        int halvarIndex = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Halvar, God of Battle");
        harness.handleCardChosen(player1, halvarIndex);

        harness.assertInHand(player1, "Halvar, God of Battle");
    }

    @Test
    void gainsLifeForManaSpentFromSnowSource() {
        harness.addToBattlefield(player1, new SnowCoveredPlains());
        harness.tapPermanent(player1, 0);
        harness.setHand(player1, List.of(new SearchForGlory()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new FearlessPup()));

        harness.setLife(player1, 10);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
    }

    @Test
    void doesNotGainLifeForManaNotFromSnowSource() {
        harness.setHand(player1, List.of(new SearchForGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new FearlessPup()));

        harness.setLife(player1, 10);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Snow-Covered Plains", "Halvar, God of Battle", "Binding the Old Gods"})
    void putsEachEligibleCategoryIntoHandAndResumesLifeGain(String chosenName) {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new SnowCoveredPlains());
            harness.tapPermanent(player1, i);
        }
        harness.setHand(player1, List.of(new SearchForGlory()));
        harness.setLibrary(player1, List.of(new SnowCoveredPlains(), new HalvarGodOfBattle(),
                new BindingTheOldGods(), new FearlessPup()));
        harness.setLife(player1, 10);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        int chosenIndex = search.params().cards().stream().map(Card::getName).toList().indexOf(chosenName);
        assertThat(chosenIndex).isNotNegative();
        harness.handleCardChosen(player1, chosenIndex);

        harness.assertInHand(player1, chosenName);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3)
                .noneMatch(card -> card.getName().equals(chosenName));
        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Search for Glory");
    }

    @Test
    void mayFailToFindAnEligibleCardAndStillGainLife() {
        harness.addToBattlefield(player1, new SnowCoveredPlains());
        harness.tapPermanent(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new SearchForGlory()));
        harness.setLibrary(player1, List.of(new HalvarGodOfBattle()));
        harness.setLife(player1, 10);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 11);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Search for Glory");
    }

    @Test
    void emptyLibraryDoesNotPreventLifeGain() {
        harness.addToBattlefield(player1, new SnowCoveredPlains());
        harness.tapPermanent(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new SearchForGlory()));
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 10);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Search for Glory");
    }
}
