package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CallTheCavalry;
import com.github.laxika.magicalvibes.cards.f.FerociousPup;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.r.RapaciousDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodlandChampion.class, CallTheCavalry.class, RaiseTheAlarm.class,
        FerociousPup.class, RapaciousDragon.class})
class WoodlandChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one +1/+1 counter on itself for each token entering together")
    void putsCountersForEachTokenEnteringTogether() {
        harness.setHand(player1, List.of(new WoodlandChampion(), new CallTheCavalry()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent champion = findPermanent(player1, "Woodland Champion");
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opposingTokensDoNotTriggerChampion() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new WoodlandChampion());
        harness.setHand(player2, List.of(new RaiseTheAlarm()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void separateTokenBatchesEachTriggerOnce() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new WoodlandChampion());
        harness.setHand(player1, List.of(new RaiseTheAlarm(), new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void countsSingleTokenButNotTheNontokenCreatureThatCreatedIt() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new WoodlandChampion());
        harness.setHand(player1, List.of(new FerociousPup()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void noncreatureTokensAlsoPutCountersOnChampion() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new WoodlandChampion());
        harness.setHand(player1, List.of(new RapaciousDragon()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
