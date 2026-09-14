package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SnuffOut;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Foster.class, FreshVolunteers.class, Island.class, SnuffOut.class})
class FosterTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} puts the first revealed creature into hand and the rest into the graveyard")
    void payingFindsCreatureAndMillsTheRest() {
        Card snuffOut = new SnuffOut();
        Card island = new Island();
        Card creature = new FreshVolunteers();
        Card deadCreature = prepareDeathTrigger(List.of(snuffOut, island, creature), true);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(deadCreature, snuffOut, island);
    }

    @Test
    @DisplayName("Declining Foster's trigger leaves the library unchanged")
    void decliningLeavesLibraryUnchanged() {
        Card snuffOut = new SnuffOut();
        Card creature = new FreshVolunteers();
        prepareDeathTrigger(List.of(snuffOut, creature), false);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(snuffOut, creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(snuffOut, creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Paying with no creature in the library puts every revealed card into the graveyard")
    void noCreatureMillsTheEntireLibrary() {
        Card snuffOut = new SnuffOut();
        Card island = new Island();
        Card deadCreature = prepareDeathTrigger(List.of(snuffOut, island), true);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(deadCreature, snuffOut, island);
    }

    @Test
    @DisplayName("A creature an opponent controls dying does not trigger Foster")
    void opponentCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new Foster());
        Card island = new Island();
        harness.setLibrary(player1, List.of(island));

        Card opponentCreature = new FreshVolunteers();
        harness.addToBattlefield(player2, opponentCreature);
        harness.setHand(player1, List.of(new SnuffOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Fresh Volunteers"));

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Accepting without enough mana does not reveal cards")
    void acceptingWithoutEnoughManaDoesNothing() {
        Card snuffOut = new SnuffOut();
        Card island = new Island();
        Card deadCreature = prepareDeathTrigger(List.of(snuffOut, island), false);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(snuffOut, island);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(snuffOut, island);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(snuffOut, island);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(deadCreature);
    }

    @Test
    @DisplayName("Paying with an empty library does not create a card in hand")
    void payingWithEmptyLibraryDoesNothing() {
        Card deadCreature = prepareDeathTrigger(List.of(), true);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(deadCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(deadCreature);
    }

    private Card prepareDeathTrigger(List<Card> library, boolean addPaymentMana) {
        harness.addToBattlefield(player1, new Foster());
        Card deadCreature = new FreshVolunteers();
        harness.addToBattlefield(player1, deadCreature);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SnuffOut()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        if (addPaymentMana) {
            harness.addMana(player1, ManaColor.COLORLESS, 1);
        }

        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Fresh Volunteers"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        return deadCreature;
    }
}
