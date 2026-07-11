package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RowanTreefolk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EverbarkShamanTest extends BaseCardTest {

    private Permanent setup(List<Card> graveyard) {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new EverbarkShaman());
        shaman.setSummoningSick(false);
        harness.setGraveyard(player1, graveyard);
        return shaman;
    }

    private int idxOf(Permanent p) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(p);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Forest(), new Forest(), new Forest(), new Island(), new GrizzlyBears()));
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
        gs.handleLibraryCardChosen(gd, player1, 0);
        gs.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(before + 2);
        long tappedForests = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest") && p.isTapped())
                .count();
        assertThat(tappedForests).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without a Treefolk card in graveyard")
    void cannotActivateWithoutTreefolkInGraveyard() {
        Permanent shaman = setup(List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(shaman), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Treefolk");
    }
}
