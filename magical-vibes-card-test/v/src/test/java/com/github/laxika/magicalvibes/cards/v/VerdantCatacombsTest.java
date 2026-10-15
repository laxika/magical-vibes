package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.Bayou;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerdantCatacombs.class, Swamp.class, Forest.class, Mountain.class,
        Plains.class, Island.class, GrizzlyBears.class})
class VerdantCatacombsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Verdant Catacombs pays 1 life and sacrifices it")
    void activationPaysLifeAndSacrificesIt() {
        harness.addToBattlefield(player1, new VerdantCatacombs());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertNotOnBattlefield(player1, "Verdant Catacombs");
        harness.assertInGraveyard(player1, "Verdant Catacombs");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Search offers only Swamp or Forest cards for the untapped battlefield")
    void searchOffersSwampOrForest() {
        activateSearch();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.getName().equals("Swamp") || card.getName().equals("Forest"))
                .containsExactlyInAnyOrderElementsOf(List.of(
                        gd.playerDecks.get(player1.getId()).get(0),
                        gd.playerDecks.get(player1.getId()).get(1)));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("Chosen Swamp or Forest enters the battlefield untapped")
    void chosenLandEntersUntapped() {
        activateSearch();

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        String chosenName = search.params().cards().getFirst().getName();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals(chosenName) && !permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Player may fail to find with Verdant Catacombs")
    void canFailToFind() {
        activateSearch();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Swamp")
                        || permanent.getCard().getName().equals("Forest"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Forest can be fetched independently of Swamp")
    void fetchesForest() {
        harness.addToBattlefield(player1, new VerdantCatacombs());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed(Bayou.class)
    @DisplayName("A nonbasic land with both eligible subtypes is offered once and can be fetched")
    void fetchesNonbasicDualLand() {
        harness.addToBattlefield(player1, new VerdantCatacombs());
        Bayou bayou = new Bayou();
        harness.setLibrary(player1, List.of(bayou, new Island()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(bayou);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Bayou");
        assertThat(findPermanent(player1, "Bayou").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bayou);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library without an eligible land finishes the search without a choice")
    void noEligibleCardsFinishesSearch() {
        harness.addToBattlefield(player1, new VerdantCatacombs());
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(island, bears));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library still allows activation and completes resolution")
    void emptyLibraryFinishesSearch() {
        harness.addToBattlefield(player1, new VerdantCatacombs());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Verdant Catacombs");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Verdant Catacombs cannot pay its tap cost")
    void tappedLandCannotActivate() {
        harness.addToBattlefieldAndReturn(player1, new VerdantCatacombs()).tap();
        int lifeBefore = gd.getLife(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player1, "Verdant Catacombs");
        assertThat(gd.stack).isEmpty();
    }

    private void activateSearch() {
        harness.addToBattlefield(player1, new VerdantCatacombs());
        setupLibrary();
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1,
                List.of(new Swamp(), new Forest(), new Mountain(), new Plains(), new Island(), new GrizzlyBears()));
    }
}
