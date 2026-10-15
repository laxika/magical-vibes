package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
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

@CardUsed({SoratamiSeer.class, Island.class, Mountain.class, WanderingOnes.class})
class SoratamiSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Pays the land return cost immediately and can use tapped lands and a tapped Seer")
    void paysCostBeforeDiscardingHand() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new SoratamiSeer());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Mountain());
        seer.tap();
        firstLand.tap();
        secondLand.tap();
        WanderingOnes originalHandCard = new WanderingOnes();
        harness.setHand(player1, List.of(originalHandCard));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(originalHandCard, firstLand.getCard(), secondLand.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(seer);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(originalHandCard, firstLand.getCard(), secondLand.getCard());
    }

    @Test
    @DisplayName("Draws no cards when the hand is empty at resolution")
    void emptyHandDrawsNothing() {
        Island firstLand = new Island();
        Island secondLand = new Island();
        firstLand.setOwnerId(player2.getId());
        secondLand.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, new SoratamiSeer());
        harness.addToBattlefield(player1, firstLand);
        harness.addToBattlefield(player1, secondLand);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new WanderingOnes()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).contains(firstLand, secondLand);
    }

    @Test
    @DisplayName("Returns two lands, then discards the hand and draws that many cards")
    void bouncesTwoLandsThenRefillsHand() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addToBattlefield(player1, new SoratamiSeer());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new WanderingOnes()));
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // The two bounced lands joined the one card already in hand: discard 3, draw 3.
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(c -> c.getName().equals("Island"));
        harness.assertInGraveyard(player1, "Wandering Ones");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Cannot be activated with fewer than two lands on the battlefield")
    void requiresTwoLands() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addToBattlefield(player1, new SoratamiSeer());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new WanderingOnes()));
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prompts which lands to return when more than two are available")
    void promptsForLandChoiceWithExtraLands() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addToBattlefield(player1, new SoratamiSeer());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, island.getId());
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot be activated without paying {4}")
    void requiresMana() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addToBattlefield(player1, new SoratamiSeer());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not count a nonland permanent toward the cost")
    void doesNotCountNonlandTowardCost() {
        harness.addToBattlefield(player1, new SoratamiSeer());
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.addToBattlefield(player1, new Island());

        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not use lands controlled by an opponent toward the cost")
    void doesNotUseOpponentsLandsForCost() {
        harness.addToBattlefield(player1, new SoratamiSeer());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Returns a controlled land to its owner's hand")
    void returnsControlledLandToItsOwner() {
        Island opponentOwnedIsland = new Island();
        opponentOwnedIsland.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, new SoratamiSeer());
        harness.addToBattlefield(player1, opponentOwnedIsland);
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(opponentOwnedIsland);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(card -> card.getName().equals("Island"));
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }
}
