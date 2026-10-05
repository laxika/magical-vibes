package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HallowedFountain;
import com.github.laxika.magicalvibes.cards.t.TorturedExistence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PilgrimOfTheAges.class, Plains.class, Forest.class, HallowedFountain.class, TorturedExistence.class})
class PilgrimOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB ability may search for a basic Plains card")
    void etbSearchesForBasicPlains() {
        harness.setHand(player1, List.of(new PilgrimOfTheAges()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        Card plains = new Plains();
        Card nonBasicPlains = new HallowedFountain();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(plains, nonBasicPlains, forest));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonBasicPlains, forest);
    }

    @Test
    @DisplayName("The graveyard ability returns Pilgrim of the Ages to its owner's hand")
    void graveyardAbilityReturnsItToHand() {
        Card pilgrim = new PilgrimOfTheAges();
        harness.setGraveyard(player1, List.of(pilgrim));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(pilgrim);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pilgrim);
    }

    @Test
    @DisplayName("Declining the ETB ability does not search")
    void decliningEtbLeavesLibraryUnchanged() {
        harness.setHand(player1, List.of(new PilgrimOfTheAges()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        Card plains = new Plains();
        harness.setLibrary(player1, List.of(plains));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(plains);
    }

    @Test
    @DisplayName("The graveyard ability returns only the activated copy")
    void graveyardAbilityReturnsOnlyItsSource() {
        Card pilgrim = new PilgrimOfTheAges();
        Card otherPilgrim = new PilgrimOfTheAges();
        harness.setGraveyard(player1, List.of(pilgrim, otherPilgrim));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(pilgrim);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherPilgrim);
    }

    @Test
    @DisplayName("The search may fail to find even when a basic Plains is available")
    void searchMayFailToFind() {
        Card plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.setHand(player1, List.of(new PilgrimOfTheAges()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching a library with no basic Plains finishes without taking a card")
    void searchWithoutMatchingCards() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new PilgrimOfTheAges()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An earlier activation cannot return a Pilgrim that left and reentered the graveyard")
    void earlierActivationCannotReturnNewGraveyardObject() {
        Card pilgrim = new PilgrimOfTheAges();
        Card otherPilgrim = new PilgrimOfTheAges();
        harness.setGraveyard(player1, List.of(pilgrim, otherPilgrim));
        harness.addToBattlefield(player1, new TorturedExistence());
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(pilgrim);

        harness.activateAbility(player1, 0, null, otherPilgrim.getId(), Zone.GRAVEYARD);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(otherPilgrim);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(pilgrim);
    }
}
