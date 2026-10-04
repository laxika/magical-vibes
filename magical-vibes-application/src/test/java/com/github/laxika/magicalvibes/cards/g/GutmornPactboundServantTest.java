package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GutmornPactboundServant.class, GrizzlyBears.class, Forest.class, Distress.class})
class GutmornPactboundServantTest extends BaseCardTest {

    @Test
    void eachPlayerDiscardsANonlandCardAndControllerDiscardConjuresDuplicateForOtherPlayer() {
        Card gutmorn = new GutmornPactboundServant();
        harness.setHand(player1, List.of(gutmorn, new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        Card duplicate = gd.playerHands.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(duplicate.getOwnerId()).isEqualTo(player2.getId());
        assertThat(gd.perpetualAnyColorManaForCastCardIds).contains(duplicate.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerForDiscardDuringAnotherPlayersTurn() {
        harness.addToBattlefield(player1, new GutmornPactboundServant());
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of(new Distress()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void opponentDiscardDuringControllersTurnConjuresDuplicateCastableWithOffColorMana() {
        harness.addToBattlefield(player1, new GutmornPactboundServant());
        Card original = new GrizzlyBears();
        harness.setHand(player1, List.of(new Distress()));
        harness.setHand(player2, List.of(original));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getId()).isNotEqualTo(original.getId());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void entersWithBothPlayersDiscardingBeforeEitherDuplicateIsConjured() {
        harness.setHand(player1, List.of(new GutmornPactboundServant(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Forest(), new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Distress");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Distress");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    void emptyAndLandOnlyHandsDoNotDiscardOrConjure() {
        harness.setHand(player1, List.of(new GutmornPactboundServant()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Forest");
    }
}
