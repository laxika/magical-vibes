package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathreapRitual.class, Forest.class})
class DeathreapRitualTest extends BaseCardTest {

    @Test
    @DisplayName("At each end step, accepting the morbid trigger draws a card")
    void drawsAtEachEndStepWhenMorbidIsMet() {
        harness.addToBattlefield(player1, new DeathreapRitual());
        setDeck(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the morbid trigger does not draw")
    void decliningTriggerDoesNotDraw() {
        harness.addToBattlefield(player1, new DeathreapRitual());
        Card topCard = new Forest();
        setDeck(player1, List.of(topCard));
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Does not trigger when no creature died this turn")
    void doesNotTriggerWithoutMorbid() {
        harness.addToBattlefield(player1, new DeathreapRitual());
        setDeck(player1, List.of(new Forest()));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
