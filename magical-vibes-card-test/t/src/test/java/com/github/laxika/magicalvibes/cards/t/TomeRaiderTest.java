package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(TomeRaider.class)
class TomeRaiderTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new TomeRaider()));
        harness.setLibrary(player1, List.of(new TomeRaider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawWaitsForTheEnterTriggerToResolve() {
        TomeRaider drawnCard = new TomeRaider();
        harness.setHand(player1, List.of(new TomeRaider()));
        harness.setLibrary(player1, List.of(drawnCard, new TomeRaider()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tome Raider");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void enteringWithoutBeingCastDrawsForItsController() {
        TomeRaider drawnCard = new TomeRaider();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new TomeRaider()));
        harness.setLibrary(player2, List.of(drawnCard, new TomeRaider()));

        harness.enterBattlefieldAndReturn(player2, new TomeRaider());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Tome Raider");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
