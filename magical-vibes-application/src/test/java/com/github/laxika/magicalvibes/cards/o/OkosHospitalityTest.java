package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({OkosHospitality.class, GrizzlyBears.class, OkoTheTrickster.class})
class OkosHospitalityTest extends BaseCardTest {

    @Test
    @DisplayName("Sets your creatures' base power and toughness to 3/3")
    void setsYourCreaturesToThreeThree() {
        Permanent ownCreature = addCreatureReady(player1);
        Permanent opposingCreature = addCreatureReady(player2);

        castAndDeclineSearch();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The base power and toughness setting wears off at end of turn")
    void settingWearsOffAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1);

        castAndDeclineSearch();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("May search finds Oko, the Trickster in the library or graveyard")
    void searchesLibraryAndGraveyardForOko() {
        Card libraryOko = new OkoTheTrickster();
        Card graveyardOko = new OkoTheTrickster();
        Card unrelatedCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryOko, unrelatedCard));
        harness.setGraveyard(player1, List.of(graveyardOko));

        castAndAcceptSearch();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(libraryOko.getId(), graveyardOko.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardOko.getId()));

        harness.assertInHand(player1, "Oko, the Trickster");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryOko, unrelatedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(graveyardOko);
    }

    @Test
    @DisplayName("Declining the optional search does not move cards")
    void decliningSearchDoesNothing() {
        Card libraryOko = new OkoTheTrickster();
        harness.setLibrary(player1, List.of(libraryOko));

        castAndDeclineSearch();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryOko);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryOko);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void castAndAcceptSearch() {
        castHospitality();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void castAndDeclineSearch() {
        castHospitality();
        harness.handleMayAbilityChosen(player1, false);
    }

    private void castHospitality() {
        harness.setHand(player1, List.of(new OkosHospitality()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, null);
        harness.passBothPriorities();
    }
}
