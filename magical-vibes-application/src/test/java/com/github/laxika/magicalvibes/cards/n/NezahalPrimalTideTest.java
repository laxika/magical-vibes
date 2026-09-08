package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NezahalPrimalTideTest extends BaseCardTest {

    @Test
    @DisplayName("Nezahal cannot be countered")
    void cannotBeCountered() {
        NezahalPrimalTide nezahal = new NezahalPrimalTide();
        harness.setHand(player1, List.of(nezahal));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, nezahal.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nezahal, Primal Tide");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Nezahal removes its controller's maximum hand size")
    void removesMaximumHandSize() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
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
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Nezahal does not trigger when an opponent casts a creature spell")
    void doesNotTriggerForCreatureSpell() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Discarding three cards exiles Nezahal and returns it tapped at the next end step")
    void discardThreeCardsExilesAndReturnsTapped() {
        harness.addToBattlefield(player1, new NezahalPrimalTide());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest(), new Forest()));
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

        Permanent nezahal = findPermanent(player1, "Nezahal, Primal Tide");
        assertThat(nezahal.isTapped()).isTrue();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
