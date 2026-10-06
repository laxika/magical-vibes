package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BogardanHellkite;
import com.github.laxika.magicalvibes.cards.p.PardicDragon;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScionOfTheUrDragon.class, PardicDragon.class, AshcoatBear.class,
        RestInPeace.class, BogardanHellkite.class})
class ScionOfTheUrDragonTest extends BaseCardTest {

    @Test
    @DisplayName("The search offers only Dragon permanent cards")
    void searchOffersOnlyDragonPermanents() {
        setUpScion();
        harness.setLibrary(player1, List.of(new PardicDragon(), new AshcoatBear()));

        activateSearch();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Pardic Dragon");
    }

    @Test
    @DisplayName("The chosen Dragon enters the graveyard and Scion copies it")
    void chosenDragonIsPutIntoGraveyardAndCopied() {
        Permanent scion = setUpScion();
        Card dragon = new BogardanHellkite();
        harness.setLibrary(player1, List.of(dragon));

        activateSearch();
        chooseSearchCard(0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dragon);
        assertThat(scion.getCard().getName()).isEqualTo("Bogardan Hellkite");
        assertThat(scion.getCard().getPower()).isEqualTo(5);
        assertThat(scion.getCard().getToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining the search does not copy Scion")
    void decliningSearchDoesNotCopy() {
        Permanent scion = setUpScion();
        Card dragon = new PardicDragon();
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
        harness.setLibrary(player1, List.of(new PardicDragon()));

        activateSearch();
        chooseSearchCard(0);
        assertThat(scion.getCard().getName()).isEqualTo("Pardic Dragon");

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
        harness.setLibrary(player1, List.of(new AshcoatBear()));

        activateSearch();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(scion.getCard().getName()).isEqualTo("Scion of the Ur-Dragon");
    }

    @Test
    @DisplayName("Scion copies the selected Dragon even when Rest in Peace exiles it instead")
    void copiesDragonDespiteGraveyardReplacement() {
        Permanent scion = setUpScion();
        harness.addToBattlefield(player2, new RestInPeace());
        Card dragon = new PardicDragon();
        harness.setLibrary(player1, List.of(dragon));

        activateSearch();
        chooseSearchCard(0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dragon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(dragon);
        assertThat(scion.getCard().getName()).isEqualTo("Pardic Dragon");
    }

    @Test
    @DisplayName("The copied Dragon's activated ability works and disappears at cleanup")
    void copiedActivatedAbilityWorksUntilCleanup() {
        Permanent scion = setUpScion();
        harness.setLibrary(player1, List.of(new PardicDragon()));
        activateSearch();
        chooseSearchCard(0);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(5);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(scion.getCard().getName()).isEqualTo("Scion of the Ur-Dragon");
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(4);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new PardicDragon()));
        activateSearch();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        chooseSearchCard(-1);
    }

    @Test
    @DisplayName("A pending activation still searches after Scion leaves the battlefield")
    void searchesAfterSourceLeavesBattlefield() {
        Permanent scion = setUpScion();
        Card dragon = new PardicDragon();
        harness.setLibrary(player1, List.of(dragon));
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(scion);
        harness.setGraveyard(player1, List.of(scion.getOriginalCard()));

        harness.passBothPriorities();
        chooseSearchCard(0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dragon);
        harness.assertNotOnBattlefield(player1, "Pardic Dragon");
    }

    @Test
    @DisplayName("Activations stacked before copying can successively copy different Dragons")
    void stackedActivationsCopyDifferentDragonsAndRevert() {
        Permanent scion = setUpScion();
        Card pardic = new PardicDragon();
        Card hellkite = new BogardanHellkite();
        harness.setLibrary(player1, List.of(pardic, hellkite));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        chooseSearchCard(0);
        assertThat(scion.getCard().getName()).isEqualTo("Pardic Dragon");

        harness.passBothPriorities();
        chooseSearchCard(0);

        assertThat(scion.getCard().getName()).isEqualTo("Bogardan Hellkite");
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pardic, hellkite);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
        harness.handleCardChosen(player1, index);
    }
}
