package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeechFanatic.class})
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

    @Test
    @DisplayName("Lifelink updates as the active player changes for both controllers")
    void lifelinkUpdatesWithActivePlayer() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LeechFanatic());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new LeechFanatic());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Unblocked combat damage gains life during its controller's turn")
    void combatDamageGainsLife() {
        Permanent fanatic = harness.addToBattlefieldAndReturn(player1, new LeechFanatic());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        fanatic.setSummoningSick(false);
        fanatic.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
