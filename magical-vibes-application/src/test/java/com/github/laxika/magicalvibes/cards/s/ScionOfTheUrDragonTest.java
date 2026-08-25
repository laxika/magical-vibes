package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScionOfTheUrDragon.class, ShivanDragon.class, GrizzlyBears.class})
class ScionOfTheUrDragonTest extends BaseCardTest {

    @Test
    @DisplayName("The search offers only Dragon permanent cards")
    void searchOffersOnlyDragonPermanents() {
        setUpScion();
        harness.setLibrary(player1, List.of(new ShivanDragon(), new GrizzlyBears()));

        activateSearch();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Shivan Dragon");
    }

    @Test
    @DisplayName("The chosen Dragon enters the graveyard and Scion copies it")
    void chosenDragonIsPutIntoGraveyardAndCopied() {
        Permanent scion = setUpScion();
        Card dragon = new ShivanDragon();
        harness.setLibrary(player1, List.of(dragon));

        activateSearch();
        chooseSearchCard(0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dragon);
        assertThat(scion.getCard().getName()).isEqualTo("Shivan Dragon");
        assertThat(scion.getCard().getPower()).isEqualTo(5);
        assertThat(scion.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining the search does not copy Scion")
    void decliningSearchDoesNotCopy() {
        Permanent scion = setUpScion();
        Card dragon = new ShivanDragon();
        harness.setLibrary(player1, List.of(dragon));

        activateSearch();
        chooseSearchCard(-1);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(dragon);
        assertThat(scion.getCard().getName()).isEqualTo("Scion of the Ur-Dragon");
    }

    @Test
    @DisplayName("The copy reverts at the end of the turn")
    void copyRevertsAtEndOfTurn() {
        Permanent scion = setUpScion();
        harness.setLibrary(player1, List.of(new ShivanDragon()));

        activateSearch();
        chooseSearchCard(0);
        assertThat(scion.getCard().getName()).isEqualTo("Shivan Dragon");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(scion.getCard().getName()).isEqualTo("Scion of the Ur-Dragon");
        assertThat(scion.getCard().getPower()).isEqualTo(4);
        assertThat(scion.getCard().getToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Finding no Dragon leaves Scion unchanged")
    void noDragonFound() {
        Permanent scion = setUpScion();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        activateSearch();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(scion.getCard().getName()).isEqualTo("Scion of the Ur-Dragon");
    }

    private Permanent setUpScion() {
        Permanent scion = harness.addToBattlefieldAndReturn(player1, new ScionOfTheUrDragon());
        scion.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        return scion;
    }

    private void activateSearch() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    private void chooseSearchCard(int index) {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
