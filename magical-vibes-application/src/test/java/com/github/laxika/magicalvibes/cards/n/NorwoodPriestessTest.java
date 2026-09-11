package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
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

@CardUsed({NorwoodPriestess.class, BearCub.class, GoblinPiker.class, Forest.class})
class NorwoodPriestessTest extends BaseCardTest {

    @Test
    @DisplayName("Putting a green creature from hand onto the battlefield taps the Priestess")
    void putsGreenCreatureOntoBattlefield() {
        setupPriestessOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BearCub()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Norwood Priestess").isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Bear Cub");
        harness.assertNotInHand(player1, "Bear Cub");
    }

    @Test
    @DisplayName("Only green creature cards are valid choices")
    void onlyGreenCreaturesAreValidChoices() {
        setupPriestessOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GoblinPiker(), new BearCub(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Ability does not offer a choice when hand holds only non-green creatures")
    void noGreenCreatureSkipsChoice() {
        setupPriestessOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GoblinPiker()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Goblin Piker");
        harness.assertInHand(player1, "Goblin Piker");
    }

    @Test
    @DisplayName("Declining the may leaves the green creature in hand")
    void decliningMayLeavesCreatureInHand() {
        setupPriestessOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BearCub()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Bear Cub");
        harness.assertInHand(player1, "Bear Cub");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate during the beginning of combat, before attackers are declared")
    void canActivateBeforeAttackers() {
        setupPriestessOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate once attackers have been declared")
    void cannotActivateAfterAttackersDeclared() {
        setupPriestessOnMyTurn(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate before attackers are declared in a later combat phase")
    void cannotActivateInLaterCombatPhase() {
        setupPriestessOnMyTurn(TurnStep.BEGINNING_OF_COMBAT);
        gd.combatPhasesThisTurn = 2;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new NorwoodPriestess());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Cannot activate when the Priestess is already tapped")
    void cannotActivateWhenAlreadyTapped() {
        Permanent priestess = setupPriestessOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        priestess.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private Permanent setupPriestessOnMyTurn(TurnStep step) {
        Permanent priestess = addCreatureReady(player1, new NorwoodPriestess());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        return priestess;
    }
}
