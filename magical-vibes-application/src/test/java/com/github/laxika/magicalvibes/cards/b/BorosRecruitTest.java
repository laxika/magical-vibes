package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElvesOfDeepShadow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorosRecruit.class, ElvesOfDeepShadow.class})
class BorosRecruitTest extends BaseCardTest {

    @Test
    @DisplayName("First strike lets Boros Recruit destroy a blocking creature before it deals combat damage")
    void firstStrikeDealsDamageBeforeBlocker() {
        Permanent recruit = addCreatureReady(player1, new BorosRecruit());
        Permanent blocker = addCreatureReady(player2, new ElvesOfDeepShadow());
        recruit.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(recruit);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }
}
