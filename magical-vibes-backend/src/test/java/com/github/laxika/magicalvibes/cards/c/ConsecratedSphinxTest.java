package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConsecratedSphinxTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    @Test
    @DisplayName("Consecrated Sphinx has ON_OPPONENT_DRAWS effect wrapped in MayEffect")
    void hasCorrectProperties() {
        ConsecratedSphinx card = new ConsecratedSphinx();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DRAWS)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DRAWS).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_OPPONENT_DRAWS).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(DrawCardEffect.class);
        DrawCardEffect drawEffect = (DrawCardEffect) mayEffect.wrapped();
        assertThat(drawEffect.amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent draw step triggers may ability for controller")
    void triggersOnOpponentDrawStep() {
        harness.addToBattlefield(player1, new ConsecratedSphinx());

        advanceToDraw(player2);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getName()).isEqualTo("Consecrated Sphinx");
    }

    @Test
    @DisplayName("Accepting the trigger draws two cards")
    void acceptingDrawsTwoCards() {
        harness.addToBattlefield(player1, new ConsecratedSphinx());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player2);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the DrawCardEffect on the stack

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
    }

    @Test
    @DisplayName("Declining the trigger does not draw any cards")
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new ConsecratedSphinx());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player2);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Controller draw step does not trigger Consecrated Sphinx")
    void doesNotTriggerOnControllerDraw() {
        harness.addToBattlefield(player1, new ConsecratedSphinx());

        advanceToDraw(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Opponent drawing two cards from a spell triggers twice")
    void triggersPerCardDrawnFromSpell() {
        harness.addToBattlefield(player1, new ConsecratedSphinx());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities(); // resolve Counsel of the Soratami (opponent draws 2)

        // Two may ability prompts are queued (one per card drawn)
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        harness.handleMayAbilityChosen(player1, true); // accept first trigger (next presented immediately)
        harness.handleMayAbilityChosen(player1, true); // accept second trigger
        harness.passBothPriorities(); // resolve one DrawCardEffect
        harness.passBothPriorities(); // resolve other DrawCardEffect

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 4);
    }

    @Test
    @DisplayName("Two Consecrated Sphinxes each trigger on opponent draw")
    void twoSphinxesStack() {
        harness.addToBattlefield(player1, new ConsecratedSphinx());
        harness.addToBattlefield(player1, new ConsecratedSphinx());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player2);

        // Both sphinxes trigger, creating 2 may ability prompts
        harness.handleMayAbilityChosen(player1, true); // accept first sphinx trigger (next presented immediately)
        harness.handleMayAbilityChosen(player1, true); // accept second sphinx trigger
        harness.passBothPriorities(); // resolve one DrawCardEffect
        harness.passBothPriorities(); // resolve other DrawCardEffect

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 4);
    }
}
