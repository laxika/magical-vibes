package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SilhanaLedgewalker;
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

@CardUsed({GhostCouncilOfOrzhova.class, SilhanaLedgewalker.class})
class GhostCouncilOfOrzhovaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes a target opponent lose 1 life and its controller gain 1 life")
    void etbDrainsTargetOpponent() {
        castGhostCouncil(player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new GhostCouncilOfOrzhova()));
        addManaForGhostCouncil();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Activation sacrifices a creature and exiles Ghost Council")
    void activationSacrificesCreatureAndExilesGhostCouncil() {
        Permanent ghostCouncil = addCreatureReady(player1, new GhostCouncilOfOrzhova());
        Permanent creature = addCreatureReady(player1, new SilhanaLedgewalker());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(ghostCouncil.getCard().getId()));
        harness.assertInGraveyard(player1, "Silhana Ledgewalker");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(ghostCouncil.getCard().getId()));
    }

    @Test
    @DisplayName("Exiled Ghost Council returns at the next end step and retriggers its ETB")
    void returnsAtNextEndStepAndRetriggersEtb() {
        addCreatureReady(player1, new GhostCouncilOfOrzhova());
        Permanent creature = addCreatureReady(player1, new SilhanaLedgewalker());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        advanceToEndStep();
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Ghost Council of Orzhova"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("A Ghost Council controlled by another player returns under its owner's control")
    void returnsUnderItsOwnersControlWhenControlledByAnotherPlayer() {
        GhostCouncilOfOrzhova ghostCouncilCard = new GhostCouncilOfOrzhova();
        ghostCouncilCard.setOwnerId(player1.getId());
        Permanent ghostCouncil = addCreatureReady(player2, ghostCouncilCard);
        Permanent creature = addCreatureReady(player2, new SilhanaLedgewalker());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(ghostCouncil.getCard().getId()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        advanceToEndStep();
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ghostCouncil.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(ghostCouncil.getCard().getId()));
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Sacrificing Ghost Council itself leaves it in the graveyard without a return")
    void canSacrificeItselfButDoesNotReturn() {
        Permanent ghostCouncil = addCreatureReady(player1, new GhostCouncilOfOrzhova());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, ghostCouncil.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ghost Council of Orzhova");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        advanceToEndStep();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ghost Council of Orzhova");
        harness.assertInGraveyard(player1, "Ghost Council of Orzhova");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A tapped Ghost Council pays the sacrifice before its ability resolves")
    void tappedGhostCouncilCanActivateAndPaysSacrificeImmediately() {
        Permanent ghostCouncil = addCreatureReady(player1, new GhostCouncilOfOrzhova());
        ghostCouncil.tap();
        Permanent creature = addCreatureReady(player1, new SilhanaLedgewalker());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());

        harness.assertInGraveyard(player1, "Silhana Ledgewalker");
        harness.assertOnBattlefield(player1, "Ghost Council of Orzhova");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ghost Council of Orzhova");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(ghostCouncil.getCard().getId()));
    }

    @Test
    @DisplayName("Activation during an end step waits for the following turn's end step")
    void activationDuringEndStepWaitsForNextEndStep() {
        Permanent ghostCouncil = addCreatureReady(player1, new GhostCouncilOfOrzhova());
        Permanent creature = addCreatureReady(player1, new SilhanaLedgewalker());
        advanceToEndStep();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ghost Council of Orzhova");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(ghostCouncil.getCard().getId()));
        assertThat(gd.stack).isEmpty();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Ghost Council of Orzhova");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ghost Council of Orzhova");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(ghostCouncil.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(ghostCouncil.getId());
        assertThat(returned.isTapped()).isFalse();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private void castGhostCouncil(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new GhostCouncilOfOrzhova()));
        addManaForGhostCouncil();
        harness.castCreature(player1, 0, targetPlayerId);
    }

    private void addManaForGhostCouncil() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
