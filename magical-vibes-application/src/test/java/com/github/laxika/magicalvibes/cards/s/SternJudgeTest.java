package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SternJudge.class, Swamp.class, Mountain.class})
class SternJudgeTest extends BaseCardTest {

    @Test
    @DisplayName("Each player loses life for the Swamps they control")
    void eachPlayerLosesLifeForOwnSwamps() {
        Permanent judge = addCreatureReady(player1, new SternJudge());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Mountain());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
        assertThat(judge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Swamp count is read when the ability resolves")
    void countsSwampsAtResolution() {
        addCreatureReady(player1, new SternJudge());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(swamp);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
