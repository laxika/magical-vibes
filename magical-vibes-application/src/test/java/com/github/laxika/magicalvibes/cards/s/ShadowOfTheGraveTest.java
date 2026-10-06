package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.w.WanderInDeath;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ShadowOfTheGrave.class, Censor.class, GrizzlyBears.class, HillGiant.class,
        MindRot.class, Terror.class, WanderInDeath.class})
class ShadowOfTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a cycled card but leaves a card that reached the graveyard by other means")
    void returnsCycledCardOnly() {
        // Grizzly Bears is already in the graveyard (put there by some other means, e.g. milled).
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Censor(), new ShadowOfTheGrave()));
        harness.setLibrary(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.BLUE, 1);      // cycling {U}
        harness.addMana(player1, ManaColor.BLACK, 1);     // Shadow of the Grave {1}{B}
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);    // cycle Censor -> graveyard
        harness.passBothPriorities();                     // resolve the cycling draw

        harness.castAndResolveInstant(player1, 0);         // Shadow of the Grave is now first in hand

        harness.assertInHand(player1, "Censor");
        harness.assertNotInGraveyard(player1, "Censor");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns nothing when no card was cycled or discarded this turn")
    void returnsNothingWithoutDiscard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ShadowOfTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnsAllDiscardedCardsIncludingNoncyclingCards() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new HillGiant(),
                new MindRot(), new ShadowOfTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 2, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Hill Giant");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Mind Rot");
        harness.assertInGraveyard(player1, "Shadow of the Grave");
    }

    @Test
    void doesNotReturnCardsDiscardedByOpponent() {
        harness.setHand(player1, List.of(new MindRot(), new ShadowOfTheGrave()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new HillGiant()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Hill Giant");
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Hill Giant");
    }

    @Test
    void doesNotReturnDiscardedCardThatLeftGraveyardAndLaterDied() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setHand(player1, List.of(bear, new Censor(), new MindRot(),
                new WanderInDeath(), new Terror(), new ShadowOfTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 11);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 2, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(bear.getId()));
        harness.passBothPriorities();
        harness.castCreature(player1, 2);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0);

        harness.assertInHand(player1, "Censor");
        harness.assertNotInGraveyard(player1, "Censor");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }
}
