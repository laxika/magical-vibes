package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.j.JinGitaxiasCoreAugur;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SundialOfTheInfinite.class, RuneclawBear.class, ActOfTreason.class, JinGitaxiasCoreAugur.class})
class SundialOfTheInfiniteTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ends the turn and passes to the next player")
    void endsTheTurn() {
        addReadySundial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID activePlayerBefore = gd.activePlayerId;
        int turnBefore = gd.turnNumber;

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isNotEqualTo(activePlayerBefore);
        assertThat(gd.turnNumber).isEqualTo(turnBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ending the turn exiles other spells from the stack")
    void exilesSpellsOnStack() {
        addReadySundial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, new ArrayList<>(List.of(new RuneclawBear())));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Runeclaw Bear"));
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Ending the turn resets until-end-of-turn modifiers")
    void resetsEndOfTurnModifiers() {
        addReadySundial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        bears.setPowerModifier(3);
        bears.setToughnessModifier(3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Taps the Sundial and spends {1} as the cost")
    void tapsSundialAsCost() {
        Permanent sundial = addReadySundial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(sundial.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadySundial(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate during opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        addReadySundial(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Discard to seven before temporary control ends during cleanup")
    void discardsBeforeReturningTemporarilyStolenJinGitaxias() {
        addReadySundial(player1);
        Permanent jin = harness.addToBattlefieldAndReturn(player2, new JinGitaxiasCoreAugur());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, jin.getId());
        harness.assertOnBattlefield(player1, "Jin-Gitaxias, Core Augur");
        harness.setHand(player1, List.of(new RuneclawBear(), new RuneclawBear(),
                new RuneclawBear(), new RuneclawBear(), new RuneclawBear(),
                new RuneclawBear(), new RuneclawBear(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.cleanupDiscardPending).isTrue();
        harness.assertOnBattlefield(player1, "Jin-Gitaxias, Core Augur");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        harness.assertOnBattlefield(player2, "Jin-Gitaxias, Core Augur");
    }

    @Test
    @DisplayName("Ending the turn requires discarding excess cards")
    void discardsDownToMaximumHandSize() {
        addReadySundial(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RuneclawBear(), new RuneclawBear(),
                new RuneclawBear(), new RuneclawBear(), new RuneclawBear(),
                new RuneclawBear(), new RuneclawBear(), new RuneclawBear()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.cleanupDiscardPending).isTrue();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("A tapped Sundial cannot activate")
    void cannotActivateWhileTapped() {
        Permanent sundial = addReadySundial(player1);
        sundial.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature Sundial can activate the turn it enters")
    void canActivateWithoutWaitingATurn() {
        harness.addToBattlefield(player1, new SundialOfTheInfinite());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.assertOnBattlefield(player1, "Sundial of the Infinite");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySundial(Player player) {
        return addCreatureReady(player, new SundialOfTheInfinite());
    }
}
