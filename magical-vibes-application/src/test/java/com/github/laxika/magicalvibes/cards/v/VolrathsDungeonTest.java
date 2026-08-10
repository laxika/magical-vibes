package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolrathsDungeonTest extends BaseCardTest {

    @Test
    @DisplayName("Any player may pay 5 life to destroy Volrath's Dungeon during their turn")
    void anyPlayerMayDestroyDungeon() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        harness.setLife(player2, 20);
        prepareMainPhase(player2);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
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
    @DisplayName("Discarding a card lets the controller put a target player's hand card on top of their library")
    void discardsAndTucksTargetHandCard() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        Card discardedCard = new GrizzlyBears();
        Card chosenCard = new Forest();
        Card remainingCard = new GrizzlyBears();
        Card oldTop = new Shock();
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

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).startsWith(chosenCard, oldTop);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("The discard ability is sorcery-speed and only accepts player targets")
    void discardAbilityRequiresSorcerySpeedAndPlayerTarget() {
        harness.addToBattlefield(player1, new VolrathsDungeon());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        prepareMainPhase(player1);
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
