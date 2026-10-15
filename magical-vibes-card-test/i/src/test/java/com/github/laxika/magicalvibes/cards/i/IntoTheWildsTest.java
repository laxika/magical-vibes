package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntoTheWilds.class, Forest.class, GiantSpider.class})
class IntoTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the top land onto the battlefield when accepted")
    void landOntoBattlefield() {
        addIntoTheWilds(player1);
        harness.setLibrary(player1, List.of(new Forest(), new GiantSpider()));

        runUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        // Giant Spider was the new top card and is drawn during the following draw step.
        harness.assertInHand(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Leaves the land on top of the library when declined")
    void declinedLeavesLandOnTop() {
        addIntoTheWilds(player1);
        harness.setLibrary(player1, List.of(new Forest(), new GiantSpider()));

        runUpkeep(player1);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Forest");
        // Left on top of the library, so it is what the draw step draws.
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Offers no choice and leaves a nonland top card on the library")
    void nonlandTopCardStaysOnTop() {
        addIntoTheWilds(player1);
        harness.setLibrary(player1, List.of(new GiantSpider(), new Forest()));

        runUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInHand(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Does not trigger on an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        addIntoTheWilds(player1);
        harness.setLibrary(player1, List.of(new Forest(), new GiantSpider()));

        runUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    private void addIntoTheWilds(Player player) {
        harness.addToBattlefield(player, new IntoTheWilds());
    }

    @Test
    @DisplayName("An empty library offers no choice during upkeep")
    void emptyLibraryOffersNoChoice() {
        addIntoTheWilds(player1);
        harness.setLibrary(player1, List.of());

        harness.withAutoStop(TurnStep.UPKEEP, () -> runUpkeep(player1));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Into the Wilds");
    }

    @Test
    @DisplayName("Looks at the top card on resolution rather than when upkeep begins")
    void usesTopCardAtResolution() {
        addIntoTheWilds(player1);
        harness.setLibrary(player1, List.of(new GiantSpider(), new Forest(), new GiantSpider()));
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        // Model another effect removing the original top card before this trigger resolves.
        gd.playerDecks.get(player1.getId()).removeFirst();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Forest"))
                .allMatch(p -> !p.isTapped());
    }

    private void runUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve the triggered ability
    }

}
