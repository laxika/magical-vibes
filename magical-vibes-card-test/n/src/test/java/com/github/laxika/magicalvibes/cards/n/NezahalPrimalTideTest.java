package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.Hornswoggle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HardyVeteran;
import com.github.laxika.magicalvibes.cards.a.AggressiveUrge;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NezahalPrimalTide.class, Hornswoggle.class, HardyVeteran.class, AggressiveUrge.class, Forest.class})
class NezahalPrimalTideTest extends BaseCardTest {

    @Test
    @DisplayName("Nezahal cannot be countered")
    void cannotBeCountered() {
        NezahalPrimalTide nezahal = new NezahalPrimalTide();
        harness.setHand(player1, List.of(nezahal));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.setHand(player2, List.of(new Hornswoggle()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, nezahal.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nezahal, Primal Tide");
        harness.assertInGraveyard(player2, "Hornswoggle");
    }

    @Test
    @DisplayName("Nezahal removes its controller's maximum hand size")
    void removesMaximumHandSize() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, new ArrayList<>(List.of(
                new HardyVeteran(), new HardyVeteran(), new HardyVeteran(), new HardyVeteran(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()
        )));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
    }

    @Test
    @DisplayName("Nezahal draws a card when an opponent casts a noncreature spell")
    void drawsWhenOpponentCastsNoncreatureSpell() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new AggressiveUrge()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Nezahal, Primal Tide"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Nezahal does not trigger when an opponent casts a creature spell")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.setHand(player2, List.of(new HardyVeteran()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Discarding three cards exiles Nezahal and returns it tapped at the next end step")
    void discardThreeCardsExilesAndReturnsTapped() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.setHand(player1, List.of(new HardyVeteran(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nezahal, Primal Tide");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Nezahal, Primal Tide"));

        advanceToEndStep();
        harness.assertNotOnBattlefield(player1, "Nezahal, Primal Tide");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent nezahal = findPermanent(player1, "Nezahal, Primal Tide");
        assertThat(nezahal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Its controller's noncreature spells do not trigger Nezahal")
    void doesNotTriggerForControllersSpell() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.setHand(player1, List.of(new AggressiveUrge()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Nezahal, Primal Tide"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The draw trigger still resolves after Nezahal is exiled in response")
    void drawTriggerSurvivesExile() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new HardyVeteran()));
        harness.setHand(player2, List.of(new AggressiveUrge()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Nezahal, Primal Tide"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Nezahal, Primal Tide");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Nezahal, Primal Tide");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hardy Veteran");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Nezahal cannot activate with fewer than three cards to discard")
    void cannotPayWithTwoCards() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Nezahal, Primal Tide");
    }

    @Test
    @DisplayName("Activating during an end step waits for the next turn's end step")
    void activationDuringEndStepWaitsForNextEndStep() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Nezahal, Primal Tide");

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);

        harness.assertNotOnBattlefield(player1, "Nezahal, Primal Tide");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Nezahal, Primal Tide").isTapped()).isTrue();
    }

    @Test
    @DisplayName("A stolen Nezahal returns tapped under its owner's control")
    void returnsToOwnerRatherThanActivator() {
        NezahalPrimalTide card = new NezahalPrimalTide();
        card.setOwnerId(player2.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nezahal, Primal Tide");
        assertThat(findPermanent(player2, "Nezahal, Primal Tide").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Nezahal does not remove its opponent's maximum hand size")
    void opponentStillDiscardsAtCleanup() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.setHand(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Nezahal's controller must discard at cleanup while it is exiled")
    void exiledNezahalDoesNotRemoveMaximumHandSize() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Nezahal, Primal Tide");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
    }
    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }
}
