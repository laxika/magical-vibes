package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.i.IntoTheFloodMaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PondProphet.class, IntoTheFloodMaw.class})
class PondProphetTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new PondProphet()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new PondProphet()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertOnBattlefield(player1, "Pond Prophet");
    }

    @Test
    void drawWaitsForTheEnterTriggerToResolve() {
        PondProphet drawnCard = new PondProphet();
        PondProphet remainingCard = new PondProphet();
        harness.setHand(player1, List.of(new PondProphet()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(drawnCard, remainingCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pond Prophet");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard, remainingCard);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void drawsEvenIfProphetLeavesBeforeItsTriggerResolves() {
        PondProphet prophet = new PondProphet();
        PondProphet drawnCard = new PondProphet();
        harness.setHand(player1, List.of(prophet));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player2, List.of(new IntoTheFloodMaw()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castInstantWithGift(player2, 0, findPermanent(player1, "Pond Prophet").getId(), false);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pond Prophet");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(prophet);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(prophet, drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
