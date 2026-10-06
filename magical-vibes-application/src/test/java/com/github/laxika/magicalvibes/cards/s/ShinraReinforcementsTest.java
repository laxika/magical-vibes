package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShinraReinforcements.class, Forest.class})
class ShinraReinforcementsTest extends BaseCardTest {

    @Test
    void entersAndMillsThreeCardsAndGainsThreeLife() {
        harness.setLibrary(player1, List.of(
                new ShinraReinforcements(), new Forest(), new ShinraReinforcements(), new Forest()));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new ShinraReinforcements(), "{2}{B}");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    void gainsFullLifeWhenLibraryHasFewerThanThreeCards() {
        Forest first = new Forest();
        ShinraReinforcements second = new ShinraReinforcements();
        harness.setLibrary(player1, List.of(first, second));
        harness.setLife(player1, 10);

        harness.castFromHand(player1, new ShinraReinforcements(), "{2}{B}");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        harness.assertLife(player1, 13);
    }

    @Test
    void gainsLifeWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new ShinraReinforcements(), "{2}{B}");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 23);
    }

    @Test
    void triggerResolvesAfterSourceLeavesAndOnlyAffectsItsController() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        Forest opponentCard = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new ShinraReinforcements(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        harness.assertLife(player1, 20);
        gd.playerBattlefields.get(player1.getId()).clear();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        harness.assertLife(player1, 23);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }
}
