package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeistySpikelingTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn")
    void hasFirstStrikeDuringControllerTurn() {
        Permanent spikeling = addCreatureReady(player1, new FeistySpikeling());
        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, spikeling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike during an opponent's turn")
    void doesNotHaveFirstStrikeDuringOpponentTurn() {
        Permanent spikeling = addCreatureReady(player1, new FeistySpikeling());
        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, spikeling, Keyword.FIRST_STRIKE)).isFalse();
    }
}
