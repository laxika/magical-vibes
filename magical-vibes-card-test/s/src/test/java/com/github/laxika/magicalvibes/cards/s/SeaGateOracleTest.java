package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.o.OvergrownBattlement;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeaGateOracle.class, NestInvader.class, OvergrownBattlement.class})
class SeaGateOracleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts the chosen card into hand and the other on the bottom")
    void etbChoosesOneForHand() {
        Card top = new NestInvader();
        Card second = new OvergrownBattlement();
        harness.setLibrary(player1, List.of(top, second));
        harness.castFromHand(player1, new SeaGateOracle(), "{2}{U}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("ETB can choose the top card instead")
    void etbCanChooseTopCard() {
        Card top = new NestInvader();
        Card second = new OvergrownBattlement();
        harness.setLibrary(player1, List.of(top, second));
        harness.castFromHand(player1, new SeaGateOracle(), "{2}{U}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(top.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("ETB puts the unchosen card below the untouched library cards")
    void unchosenCardGoesBelowUntouchedCards() {
        Card top = new NestInvader();
        Card second = new OvergrownBattlement();
        Card third = new NestInvader();
        Card fourth = new OvergrownBattlement();
        harness.setLibrary(player1, List.of(top, second, third, fourth));

        harness.castFromHand(player1, new SeaGateOracle(), "{2}{U}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, fourth, top);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB puts the only library card into hand without a choice")
    void oneCardLibrary() {
        Card onlyCard = new NestInvader();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.castFromHand(player1, new SeaGateOracle(), "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB resolves normally with an empty library")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new SeaGateOracle(), "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }
}
