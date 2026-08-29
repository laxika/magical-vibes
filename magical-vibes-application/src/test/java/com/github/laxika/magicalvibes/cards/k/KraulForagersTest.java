package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    private void castKraulForagers() {
        harness.setHand(player1, List.of(new KraulForagers()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
