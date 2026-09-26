package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeerDog.class, HonorGuard.class})
class DeerDogTest extends BaseCardTest {

    @Test
    @DisplayName("First strike deals combat damage before a 1/1 blocker")
    void firstStrikeDealsDamageFirst() {
        Permanent attacker = addCreatureReady(player1, new DeerDog());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new HonorGuard());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Deer-Dog");
        harness.assertInGraveyard(player2, "Honor Guard");
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
    }
}
