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

    private void castDrowner() {
        harness.setHand(player1, List.of(new GurmagDrowner()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
