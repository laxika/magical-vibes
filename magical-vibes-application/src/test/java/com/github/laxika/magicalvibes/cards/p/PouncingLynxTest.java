package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PouncingLynx.class)
class PouncingLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn")
    void hasFirstStrikeDuringControllersTurn() {
        Permanent lynx = addCreatureReady(player1, new PouncingLynx());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, lynx, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike during its controller's opponent's turn")
    void noFirstStrikeDuringOpponentsTurn() {
        Permanent lynx = addCreatureReady(player1, new PouncingLynx());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, lynx, Keyword.FIRST_STRIKE)).isFalse();
    }
}
