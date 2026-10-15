package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({KraulForagers.class, GrizzlyBears.class, Forest.class})
class KraulForagersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 1 life for each creature card in its controller's graveyard")
    void etbGainsLifePerCreatureCardInControllerGraveyard() {
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        castKraulForagers();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("ETB gains no life when its controller has no creature cards in their graveyard")
    void etbGainsNoLifeWithoutCreatureCardsInControllerGraveyard() {
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castKraulForagers();

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Undergrowth counts creature cards added after the creature enters")
    void countsCreatureCardsAtResolution() {
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of());

        castKraulForagersLeavingTriggerOnStack();
        harness.assertLife(player1, 10);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        resolveAllTriggers();

        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Undergrowth does not count creature cards removed before resolution")
    void ignoresCreatureCardsRemovedBeforeResolution() {
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        castKraulForagersLeavingTriggerOnStack();
        harness.setGraveyard(player1, List.of(new Forest()));
        resolveAllTriggers();

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("An opponent's Kraul Foragers gains life for that opponent only")
    void opponentGainsLifeFromTheirOwnGraveyard() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new KraulForagers()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 11);
    }

    private void castKraulForagers() {
        castKraulForagersLeavingTriggerOnStack();
        resolveAllTriggers();
    }

    private void castKraulForagersLeavingTriggerOnStack() {
        harness.setHand(player1, List.of(new KraulForagers()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
