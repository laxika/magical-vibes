package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BorborygmosEnraged.class, Forest.class, GrizzlyBears.class, Island.class, Shock.class})
class BorborygmosEnragedTest extends BaseCardTest {

    private void attackWithBorborygmos() {
        Permanent borborygmos = addCreatureReady(player1, new BorborygmosEnraged());
        borborygmos.setAttacking(true);
    }

    private void stackTop(List<Card> cards) {
        List<Card> deck = new ArrayList<>(cards);
        deck.addAll(gd.playerDecks.get(player1.getId()));
        harness.setLibrary(player1, deck);
    }

    @Test
    @DisplayName("Combat damage to a player puts revealed lands into hand and the rest into the graveyard")
    void combatDamageRevealsThree() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card island = new Island();
        stackTop(List.of(forest, bears, island));
        attackWithBorborygmos();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, island);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no lands among the top three, all revealed cards go to the graveyard")
    void combatDamageNoLands() {
        Card shock = new Shock();
        Card bears1 = new GrizzlyBears();
        Card bears2 = new GrizzlyBears();
        stackTop(List.of(shock, bears1, bears2));
        attackWithBorborygmos();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(shock, bears1, bears2);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Discarding a land deals 3 damage to a player")
    void discardLandDealsDamageToPlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new BorborygmosEnraged());
        harness.setHand(player1, List.of(new Forest()));
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 3);
    }

    @Test
    @DisplayName("Discarding a land deals 3 damage to a creature")
    void discardLandDealsDamageToCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new BorborygmosEnraged());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate the ability without a land card in hand")
    void cannotActivateWithoutLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new BorborygmosEnraged());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("All three revealed lands are mandatory and the fourth card stays in the library")
    void combatDamageAllLands() {
        Card forest1 = new Forest();
        Card island = new Island();
        Card forest2 = new Forest();
        Card fourth = new Shock();
        stackTop(List.of(forest1, island, forest2, fourth));
        attackWithBorborygmos();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest1, island, forest2);
        assertThat(gd.playerDecks.get(player1.getId())).startsWith(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest1, island, forest2, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with fewer than three cards reveals and moves only the available cards")
    void combatDamageShortLibrary() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, bears));
        attackWithBorborygmos();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent the combat damage trigger from resolving")
    void combatDamageEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        List<Card> handBefore = new ArrayList<>(gd.playerHands.get(player1.getId()));
        attackWithBorborygmos();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Borborygmos can discard multiple lands without revealing cards")
    void repeatedActivationsDoNotRequireTappingOrTriggerReveal() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent borborygmos = harness.addToBattlefieldAndReturn(player1, new BorborygmosEnraged());
        borborygmos.setSummoningSick(true);
        borborygmos.setTapped(true);
        Card forest = new Forest();
        Card island = new Island();
        harness.setHand(player1, List.of(forest, island));
        List<Card> libraryBefore = new ArrayList<>(gd.playerDecks.get(player1.getId()));
        int startingLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(startingLife - 6);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, island);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(libraryBefore);
        assertThat(borborygmos.isTapped()).isTrue();
    }
}
