package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalconAndRedwing.class, GrizzlyBears.class})
class FalconAndRedwingTest extends BaseCardTest {

    @Test
    void createsBirdsEqualToCombatDamageAndGrows() {
        Permanent falcon = addCreatureReady(player1, new FalconAndRedwing());
        falcon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        falcon.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).hasSize(3)
                .allSatisfy(bird -> {
                    assertThat(bird.getCard().getPower()).isEqualTo(1);
                    assertThat(bird.getCard().getToughness()).isEqualTo(1);
                    assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
                });
        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerWhenBlocked() {
        Permanent falcon = addCreatureReady(player1, new FalconAndRedwing());
        falcon.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Bird")).isEmpty();
        assertThat(falcon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
