package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GurmagDrowner.class, GrizzlyBears.class, Shock.class, Forest.class, Island.class})
class GurmagDrownerTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit does not trigger the library ability")
    void decliningExploitDoesNotTriggerLibraryAbility() {
        castDrowner();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Gurmag Drowner");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exploiting a creature puts one of the top four cards into hand and the rest into the graveyard")
    void exploitSelectsOneCardAndPutsTheRestIntoGraveyard() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card chosen = new Shock();
        Card restOne = new Forest();
        Card restTwo = new Island();
        Card restThree = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo, restThree));

        castDrowner();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(restOne, restTwo, restThree);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exploiting itself selects only from the top four cards")
    void exploitingItselfLooksAtOnlyTopFourCards() {
        Card chosen = new Island();
        Card first = new Forest();
        Card second = new Island();
        Card third = new Forest();
        Card untouched = new Island();
        harness.setLibrary(player1, List.of(first, second, chosen, third, untouched));

        exploitDrownerItself();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        harness.assertNotOnBattlefield(player1, "Gurmag Drowner");
        harness.assertInGraveyard(player1, "Gurmag Drowner");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third)
                .doesNotContain(chosen, untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A two-card library gives one card to hand and one to the graveyard")
    void shortLibraryStillPutsOneCardIntoHand() {
        Card chosen = new Island();
        Card rest = new Forest();
        harness.setLibrary(player1, List.of(rest, chosen));

        exploitDrownerItself();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rest).doesNotContain(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The only library card goes to hand without a selection")
    void oneCardLibraryPutsOnlyCardIntoHand() {
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));

        exploitDrownerItself();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Looking at an empty library does not draw or lose the game")
    void emptyLibraryDoesNotLoseGame() {
        harness.setLibrary(player1, List.of());

        exploitDrownerItself();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing no card is illegal when cards are available")
    void cannotPutAllLookedAtCardsIntoGraveyard() {
        Card first = new Island();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        exploitDrownerItself();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second).doesNotContain(first);
    }

    private void exploitDrownerItself() {
        castDrowner();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Gurmag Drowner"));
        harness.passBothPriorities();
    }

    private void castDrowner() {
        harness.setHand(player1, List.of(new GurmagDrowner()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
