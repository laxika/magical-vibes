package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
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

@CardUsed({DeathreapRitual.class, Forest.class, SakuraTribeElder.class})
class DeathreapRitualTest extends BaseCardTest {

    @Test
    @DisplayName("At each end step, accepting the morbid trigger draws a card")
    void drawsAtEachEndStepWhenMorbidIsMet() {
        harness.addToBattlefield(player1, new DeathreapRitual());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the morbid trigger does not draw")
    void decliningTriggerDoesNotDraw() {
        harness.addToBattlefield(player1, new DeathreapRitual());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
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
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature dying before Ritual enters still enables the opponent's end step draw")
    void deathBeforeRitualEntersCounts() {
        SakuraTribeElder creature = new SakuraTribeElder();
        harness.addToBattlefieldAndReturn(player2, creature).setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);

        harness.addToBattlefield(player1, new DeathreapRitual());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(topCard);
    }

    @Test
    @DisplayName("Multiple creatures dying still offer only one draw at the controller's end step")
    void multipleDeathsDrawOnlyOneCard() {
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new DeathreapRitual());
        SakuraTribeElder ownCreature = new SakuraTribeElder();
        SakuraTribeElder opponentCreature = new SakuraTribeElder();
        harness.addToBattlefieldAndReturn(player1, ownCreature).setMarkedDamage(1);
        harness.addToBattlefieldAndReturn(player2, opponentCreature).setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCreature);
        Forest topCard = new Forest();
        Forest secondCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.handleMayAbilityChosen(player1, true));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature dying after the end step begins does not trigger Ritual retroactively")
    void deathDuringEndStepIsTooLate() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new DeathreapRitual());
        var creature = harness.addToBattlefieldAndReturn(player2, new SakuraTribeElder());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player2);
        assertThat(gd.stack).isEmpty();
        creature.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
    }

}
