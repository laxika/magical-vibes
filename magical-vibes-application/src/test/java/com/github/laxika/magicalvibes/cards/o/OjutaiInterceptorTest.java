package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BreakOpen;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OjutaiInterceptor.class, BreakOpen.class})
class OjutaiInterceptorTest extends BaseCardTest {

    @Test
    void megamorphPutsPlusOneCounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new OjutaiInterceptor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent interceptor = findPermanent(player1, "Ojutai Interceptor");
        assertThat(interceptor.isFaceDown()).isTrue();
        assertThat(interceptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(interceptor));

        assertThat(interceptor.isFaceDown()).isFalse();
        assertThat(interceptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed({OjutaiInterceptor.class, BreakOpen.class})
    void turningFaceUpWithSpellDoesNotGrantMegamorphCounter() {
        Permanent interceptor = harness.addToBattlefieldAndReturn(player2, new OjutaiInterceptor());
        interceptor.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new BreakOpen()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, interceptor.getId());

        assertThat(interceptor.isFaceDown()).isFalse();
        assertThat(interceptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castingFaceUpDoesNotGrantMegamorphCounter() {
        harness.setHand(player1, List.of(new OjutaiInterceptor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent interceptor = findPermanent(player1, "Ojutai Interceptor");
        assertThat(interceptor.isFaceDown()).isFalse();
        assertThat(interceptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void groundCreatureCannotBlockUntilInterceptorIsFaceDown() {
        Permanent attacker = addCreatureReady(player1, new OjutaiInterceptor());
        Permanent blocker = addCreatureReady(player2, new OjutaiInterceptor());
        blocker.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThat(bls.canBlockAttacker(gd, blocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
