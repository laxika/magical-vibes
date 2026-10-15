package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RowanTreefolk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EverbarkShaman.class, Forest.class, GrizzlyBears.class, Island.class, RowanTreefolk.class})
class EverbarkShamanTest extends BaseCardTest {

    private Permanent setup(List<Card> graveyard) {
        Permanent shaman = addCreatureReady(player1, new EverbarkShaman());
        harness.setGraveyard(player1, graveyard);
        return shaman;
    }

    private int idxOf(Permanent p) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(p);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Island(), new GrizzlyBears()));
    }

    @Test
    @DisplayName("Activating prompts to choose a Treefolk card to exile")
    void promptsForTreefolkExile() {
        Permanent shaman = setup(List.of(new RowanTreefolk()));

        harness.activateAbility(player1, idxOf(shaman), null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    @DisplayName("Only Treefolk cards are valid to exile as the cost")
    void onlyTreefolkCardsAreValid() {
        // index 0 non-Treefolk (Grizzly Bears), index 1 Treefolk (Rowan Treefolk)
        Permanent shaman = setup(List.of(new GrizzlyBears(), new RowanTreefolk()));

        harness.activateAbility(player1, idxOf(shaman), null, null);

        PendingInteraction.GraveyardExileCostChoice choice =
                (PendingInteraction.GraveyardExileCostChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Exiles the chosen Treefolk and offers only Forest cards to search")
    void exilesTreefolkAndOffersForests() {
        Permanent shaman = setup(List.of(new RowanTreefolk()));
        setupLibrary();

        harness.activateAbility(player1, idxOf(shaman), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Rowan Treefolk"));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Picking two Forests puts them onto the battlefield tapped")
    void pickingTwoForestsPutsThemOnBattlefieldTapped() {
        Permanent shaman = setup(List.of(new RowanTreefolk()));
        setupLibrary();

        harness.activateAbility(player1, idxOf(shaman), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        int before = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(before + 2);
        assertThat(findPermanents(player1, "Forest"))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cannot activate without a Treefolk card in graveyard")
    void cannotActivateWithoutTreefolkInGraveyard() {
        Permanent shaman = setup(List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(shaman), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Treefolk");
    }

    @Test
    @DisplayName("The Treefolk is exiled and the Shaman tapped before the ability resolves")
    void paysCostsBeforeResolution() {
        RowanTreefolk treefolk = new RowanTreefolk();
        Permanent shaman = setup(List.of(treefolk));
        setupLibrary();

        harness.activateAbility(player1, idxOf(shaman), null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(shaman.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(treefolk);
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
    }

    @Test
    @DisplayName("May find zero Forests even when Forests are available")
    void mayFindZeroForests() {
        Permanent shaman = setup(List.of(new RowanTreefolk()));
        setupLibrary();
        List<Card> originalLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));

        harness.activateAbility(player1, idxOf(shaman), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(originalLibrary);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May stop after finding one Forest when more are available")
    void mayFindOnlyOneForest() {
        Permanent shaman = setup(List.of(new RowanTreefolk()));
        setupLibrary();

        harness.activateAbility(player1, idxOf(shaman), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Forest")).hasSize(1).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Finding the only Forest completes the search")
    void onlyOneForestAvailable() {
        Permanent shaman = setup(List.of(new RowanTreefolk()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        harness.activateAbility(player1, idxOf(shaman), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Forest")).hasSize(1).allMatch(Permanent::isTapped);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A search with no Forests completes without moving other cards")
    void noForestsAvailable() {
        Permanent shaman = setup(List.of(new RowanTreefolk()));
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(island, bears));

        harness.activateAbility(player1, idxOf(shaman), null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(shaman);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot use a Treefolk in the opponent's graveyard to pay the cost")
    void cannotExileOpponentsTreefolk() {
        Permanent shaman = setup(List.of());
        RowanTreefolk treefolk = new RowanTreefolk();
        harness.setGraveyard(player2, List.of(treefolk));

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(shaman), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shaman.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(treefolk);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Shaman cannot activate the ability")
    void cannotActivateWhileTapped() {
        Permanent shaman = setup(List.of(new RowanTreefolk()));
        shaman.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(shaman), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Shaman cannot activate the tap ability")
    void cannotActivateWithSummoningSickness() {
        Permanent shaman = setup(List.of(new RowanTreefolk()));
        shaman.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(shaman), null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(shaman.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
