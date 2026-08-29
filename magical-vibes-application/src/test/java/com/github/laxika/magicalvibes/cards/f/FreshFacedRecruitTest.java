package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FreshFacedRecruitTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn")
    void hasFirstStrikeDuringControllersTurn() {
        Permanent recruit = addCreatureReady(player1, new FreshFacedRecruit());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike during its controller's opponent's turn")
    void noFirstStrikeDuringOpponentsTurn() {
        Permanent recruit = addCreatureReady(player1, new FreshFacedRecruit());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FIRST_STRIKE)).isFalse();
    }
}
