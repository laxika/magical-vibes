package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolrathsDungeon.class, RagingGoblin.class})
class VolrathsDungeonTest extends BaseCardTest {

    @Test
    @DisplayName("Any player may pay 5 life to destroy Volrath's Dungeon during their turn")
    void anyPlayerMayDestroyDungeon() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        harness.setLife(player2, 20);
        prepareMainPhase(player2);

        harness.activateAbility(player2, 0, null, null);

        harness.assertLife(player2, 15);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Volrath's Dungeon");
        harness.assertInGraveyard(player1, "Volrath's Dungeon");
    }

    @Test
    @DisplayName("The life-payment ability cannot be activated during another player's turn")
    void destroyAbilityRequiresActivatingPlayerTurn() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discarding a card makes the target player choose a hand card to put on top of their library")
    void discardsAndTucksTargetHandCard() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        Card discardedCard = new RagingGoblin();
        Card chosenCard = new RagingGoblin();
        Card remainingCard = new RagingGoblin();
        Card oldTop = new RagingGoblin();
        harness.setHand(player1, List.of(discardedCard));
        harness.setHand(player2, List.of(chosenCard, remainingCard));
        harness.setLibrary(player2, List.of(oldTop));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player2, List.of(chosenCard.getId()));

        harness.assertInGraveyard(player1, "Raging Goblin");
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(chosenCard, oldTop);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("The discard ability can only be activated by the Dungeon's controller")
    void discardAbilityRequiresController() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        harness.setHand(player2, List.of(new RagingGoblin()));
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty target hand makes the discard ability resolve without a card choice")
    void emptyTargetHandNeedsNoChoice() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        Card discardedCard = new RagingGoblin();
        Card oldTop = new RagingGoblin();
        harness.setHand(player1, List.of(discardedCard));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(oldTop));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Raging Goblin");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(oldTop);
    }

    @Test
    @DisplayName("The discard ability is sorcery-speed and only accepts player targets")
    void discardAbilityRequiresSorcerySpeedAndPlayerTarget() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        harness.setHand(player1, List.of(new RagingGoblin()));
        prepareMainPhase(player1);
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying life requires at least five life and does not destroy the Dungeon as a cost")
    void insufficientLifeCannotPayDestroyCost() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        harness.setLife(player2, 4);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 4);
        harness.assertOnBattlefield(player1, "Volrath's Dungeon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller may pay life during upkeep and destruction waits for resolution")
    void controllerCanDestroyDungeonOutsideMainPhase() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        harness.setLife(player1, 20);
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 15);
        harness.assertOnBattlefield(player1, "Volrath's Dungeon");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Volrath's Dungeon");
        harness.assertInGraveyard(player1, "Volrath's Dungeon");
    }

    @Test
    @DisplayName("The controller may target themselves after discarding a different card")
    void discardAbilityCanTargetController() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        Card discardedCard = new RagingGoblin();
        Card chosenCard = new RagingGoblin();
        Card oldTop = new RagingGoblin();
        harness.setHand(player1, List.of(discardedCard, chosenCard));
        harness.setLibrary(player1, List.of(oldTop));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).startsWith(chosenCard, oldTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty controller hand cannot pay the discard cost")
    void discardAbilityRequiresCardToDiscard() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new RagingGoblin()));
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("Destroying the Dungeon in response does not stop its discard ability")
    void discardAbilityResolvesAfterSourceIsDestroyed() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        harness.setLife(player1, 20);
        Card discardedCard = new RagingGoblin();
        Card chosenCard = new RagingGoblin();
        Card oldTop = new RagingGoblin();
        harness.setHand(player1, List.of(discardedCard));
        harness.setHand(player2, List.of(chosenCard));
        harness.setLibrary(player2, List.of(oldTop));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Volrath's Dungeon");
        harness.assertLife(player1, 15);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(chosenCard.getId()));

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(chosenCard, oldTop);
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
