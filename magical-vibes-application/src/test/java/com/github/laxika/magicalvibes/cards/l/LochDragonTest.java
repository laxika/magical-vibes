package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LochDragon.class, Forest.class})
class LochDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Entering lets Loch Dragon discard a card to draw a card")
    void enteringDiscardsThenDraws() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Card discarded = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new LochDragon(), discarded)));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Attacking lets Loch Dragon discard a card to draw a card")
    void attackingDiscardsThenDraws() {
        addCreatureReady(player1, new LochDragon());
        Card discarded = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining Loch Dragon's trigger does not discard or draw")
    void decliningTriggerDoesNothing() {
        addCreatureReady(player1, new LochDragon());
        Card retained = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(retained)));
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting with an empty hand does not draw a card")
    void emptyHandDoesNotDraw() {
        addCreatureReady(player1, new LochDragon());
        harness.setHand(player1, List.of());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The attacking Dragon's controller discards and draws")
    void opponentControlledDragonUsesOpponentsHandAndLibrary() {
        addCreatureReady(player2, new LochDragon());
        Card discarded = new Forest();
        Card drawn = new Forest();
        Card retained = new Forest();
        Card untouchedTop = new Forest();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(drawn));
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(untouchedTop));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouchedTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
