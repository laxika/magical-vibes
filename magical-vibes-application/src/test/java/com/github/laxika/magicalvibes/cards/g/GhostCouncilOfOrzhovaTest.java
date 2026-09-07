package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostCouncilOfOrzhova.class, GrizzlyBears.class})
class GhostCouncilOfOrzhovaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes a target opponent lose 1 life and its controller gain 1 life")
    void etbDrainsTargetOpponent() {
        castGhostCouncil(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new GhostCouncilOfOrzhova()));
        addManaForGhostCouncil();

        assertThatThrownBy(() -> harness.getGameService().playCard(
                gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Activation sacrifices a creature and exiles Ghost Council")
    void activationSacrificesCreatureAndExilesGhostCouncil() {
        Permanent ghostCouncil = harness.addToBattlefieldAndReturn(player1, new GhostCouncilOfOrzhova());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(ghostCouncil.getCard().getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(ghostCouncil.getCard().getId()));
    }

    @Test
    @DisplayName("Exiled Ghost Council returns at the next end step and retriggers its ETB")
    void returnsAtNextEndStepAndRetriggersEtb() {
        harness.addToBattlefield(player1, new GhostCouncilOfOrzhova());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        advanceToEndStep();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Ghost Council of Orzhova"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    private void castGhostCouncil(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new GhostCouncilOfOrzhova()));
        addManaForGhostCouncil();
        harness.getGameService().playCard(gd, player1, 0, 0, targetPlayerId, null);
    }

    private void addManaForGhostCouncil() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
