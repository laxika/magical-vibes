package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicSellSword.class, GrizzlyBears.class})
class AngelicSellSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a Mercenary token")
    void enteringCreatesMercenaryToken() {
        harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(1);
    }

    @Test
    @DisplayName("Another nontoken creature entering creates a Mercenary token")
    void anotherNontokenCreatureEnteringCreatesMercenaryToken() {
        harness.enterBattlefieldAndReturn(player1, new AngelicSellSword());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mercenary")).hasSize(2);
    }

    @Test
    @DisplayName("Attacking draws a card when power is at least six")
    void attackingWithSixPowerDraws() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new AngelicSellSword());
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        angel.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Attacking does not draw a card when power is below six")
    void attackingBelowSixPowerDoesNotDraw() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new AngelicSellSword());
        angel.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Card()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
