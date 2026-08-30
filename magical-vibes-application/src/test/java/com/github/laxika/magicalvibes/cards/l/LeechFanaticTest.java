package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LeechFanaticTest extends BaseCardTest {

    @Test
    @DisplayName("Has lifelink during its controller's turn")
    void hasLifelinkDuringControllerTurn() {
        Permanent fanatic = harness.addToBattlefieldAndReturn(player1, new LeechFanatic());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, fanatic, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not have lifelink during an opponent's turn")
    void doesNotHaveLifelinkDuringOpponentTurn() {
        Permanent fanatic = harness.addToBattlefieldAndReturn(player1, new LeechFanatic());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, fanatic, Keyword.LIFELINK)).isFalse();
    }
}
