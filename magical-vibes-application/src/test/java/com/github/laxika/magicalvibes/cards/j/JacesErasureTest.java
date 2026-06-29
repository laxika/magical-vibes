package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JacesErasureTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Jace's Erasure has MayEffect wrapping MillTargetPlayerEffect(1) on ON_CONTROLLER_DRAWS")
    void hasCorrectEffects() {
        JacesErasure card = new JacesErasure();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_DRAWS)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_DRAWS).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_CONTROLLER_DRAWS).getFirst();
        assertThat(may.wrapped()).isInstanceOf(MillTargetPlayerEffect.class);
        MillTargetPlayerEffect mill = (MillTargetPlayerEffect) may.wrapped();
        assertThat(mill.count()).isEqualTo(1);
    }

    // ===== Draw step trigger — accept and target opponent =====

    @Test
    @DisplayName("Draw step draw triggers may prompt, accepting mills target opponent 1 card")
    void drawStepTriggersMillOpponent() {
        harness.addToBattlefield(player1, new JacesErasure());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardSizeBefore = gd.playerGraveyards.get(player2.getId()).size();

        advanceToDraw(player1);

        // Resolve MayEffect from stack
        harness.passBothPriorities();

        // May prompt should be awaiting input
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        // Accept the may ability — target selection happens inline
        harness.handleMayAbilityChosen(player1, true);

        // Should prompt for target player selection
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Choose opponent as target — mill resolves inline
        harness.handlePermanentChosen(player1, player2.getId());

        // Opponent should have milled 1 card
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId()).size()).isEqualTo(graveyardSizeBefore + 1);
    }

    // ===== Draw step trigger — accept and target self =====

    @Test
    @DisplayName("Accepting and targeting self mills controller 1 card")
    void drawStepTriggersMillSelf() {
        harness.addToBattlefield(player1, new JacesErasure());
        // After draw step, player1 has drawn a card, so we need to track deck size after draw
        advanceToDraw(player1);

        int deckSizeAfterDraw = gd.playerDecks.get(player1.getId()).size();
        int graveyardSizeAfterDraw = gd.playerGraveyards.get(player1.getId()).size();

        // Resolve MayEffect from stack
        harness.passBothPriorities();

        // Accept the may ability — target selection happens inline
        harness.handleMayAbilityChosen(player1, true);

        // Choose self as target — mill resolves inline
        harness.handlePermanentChosen(player1, player1.getId());

        // Controller should have milled 1 card
        assertThat(gd.playerDecks.get(player1.getId()).size()).isEqualTo(deckSizeAfterDraw - 1);
        assertThat(gd.playerGraveyards.get(player1.getId()).size()).isEqualTo(graveyardSizeAfterDraw + 1);
    }

    // ===== Draw step trigger — decline =====

    @Test
    @DisplayName("Declining may ability does not mill anyone")
    void declineDoesNotMill() {
        harness.addToBattlefield(player1, new JacesErasure());
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToDraw(player1);

        // Resolve MayEffect from stack
        harness.passBothPriorities();

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // Opponent's deck unchanged
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(opponentDeckBefore);
    }

    // ===== Spell-based draw triggers =====

    @Test
    @DisplayName("Drawing from a spell triggers the may ability")
    void spellDrawTriggersMayPrompt() {
        harness.addToBattlefield(player1, new JacesErasure());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Counsel of the Soratami draws 2 cards
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve Counsel (draws 2, triggers 2 MayEffects on stack)

        // Resolve first MayEffect (top of stack) -> MAY prompt
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId()); // mill resolves inline

        // Resolve second MayEffect -> MAY prompt
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId()); // mill resolves inline

        // Opponent should have milled 2 cards total
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(opponentDeckBefore - 2);
    }

    // ===== Opponent's draw does not trigger =====

    @Test
    @DisplayName("Opponent's draw does not trigger Jace's Erasure")
    void doesNotTriggerOnOpponentDraw() {
        harness.addToBattlefield(player1, new JacesErasure());
        int opponentDeckBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToDraw(player2);

        // No may prompt for player1
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();

        // Opponent's deck only lost the card they drew
        assertThat(gd.playerDecks.get(player2.getId()).size()).isEqualTo(opponentDeckBefore - 1);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }
}
