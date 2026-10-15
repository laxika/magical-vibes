package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GallantCavalry;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AjanisWelcome.class, GreenwoodSentinel.class, GallantCavalry.class})
class AjanisWelcomeTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life when a creature you control enters")
    void gainsLifeOnAllyCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AjanisWelcome());
        harness.castFromHand(player1, new GreenwoodSentinel(), "{1}{G}");
        harness.passBothPriorities(); // resolve creature

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve life trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Does not gain life when an opponent's creature enters")
    void noLifeOnOpponentCreatureEnter() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AjanisWelcome());
        harness.enterBattlefieldAndReturn(player2, new GreenwoodSentinel());

        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Gains life for both a creature spell and its creature token")
    void gainsLifeForCreatureAndToken() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AjanisWelcome());

        harness.castFromHand(player1, new GallantCavalry(), "{3}{W}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each copy triggers separately for an entering creature")
    void multipleCopiesEachGainLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AjanisWelcome());
        harness.addToBattlefield(player1, new AjanisWelcome());

        harness.castFromHand(player1, new GreenwoodSentinel(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("An entering noncreature enchantment does not trigger life gain")
    void noncreatureEntryDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AjanisWelcome());

        harness.castFromHand(player1, new AjanisWelcome(), "{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
