package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaryJaneWatson.class, GiantSpider.class, GrizzlyBears.class, Forest.class})
class MaryJaneWatsonTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a Spider you control enters")
    void drawsWhenControlledSpiderEnters() {
        harness.addToBattlefield(player1, new MaryJaneWatson());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        castCreature(player1, new GiantSpider());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("Does not trigger for a non-Spider or an opponent's Spider")
    void onlyTriggersForControlledSpiders() {
        harness.addToBattlefield(player1, new MaryJaneWatson());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        castCreature(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantSpider()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new MaryJaneWatson());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        castCreature(player1, new GiantSpider());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);

        castCreature(player1, new GiantSpider());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Triggers again on a later turn")
    void triggersAgainOnLaterTurn() {
        harness.addToBattlefield(player1, new MaryJaneWatson());
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        castCreature(player1, new GiantSpider());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);

        advanceTurn();
        advanceTurn();

        castCreature(player1, new GiantSpider());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(third);
    }

    private void castCreature(Player player, Card card) {
        harness.setHand(player, List.of(card));
        harness.addMana(player, ManaColor.GREEN, 4);
        harness.castCreature(player, 0);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
