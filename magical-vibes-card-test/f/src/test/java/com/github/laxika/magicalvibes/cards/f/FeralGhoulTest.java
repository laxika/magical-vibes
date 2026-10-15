package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AnguishedUnmaking;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeralGhoul.class, GrizzlyBears.class, Shock.class, AnguishedUnmaking.class})
class FeralGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature you control dying puts a +1/+1 counter on Feral Ghoul")
    void allyCreatureDeathPutsCountersOnFeralGhoul() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new FeralGhoul());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When Feral Ghoul dies, each opponent gets rad counters equal to its power")
    void selfDeathGivesEachOpponentRadCountersEqualToPower() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new FeralGhoul());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, ghoul.getId());
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player1.getId())).isNull();
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void opponentCreatureDeathDoesNotAddCounter() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new FeralGhoul());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerRadCounters).isEmpty();
    }

    @Test
    void deathUsesPowerIncludingCountersEarnedFromAllyDeaths() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new FeralGhoul());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        resolveAllTriggers();
        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castAndResolveInstant(player2, 0, ghoul.getId());
        assertThat(gd.playerRadCounters).isEmpty();
        harness.castAndResolveInstant(player2, 0, ghoul.getId());
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player1.getId())).isNull();
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void exilingAllyAndGhoulDoesNotTriggerDeathAbilities() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new FeralGhoul());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new AnguishedUnmaking(), new AnguishedUnmaking()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        resolveAllTriggers();
        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castAndResolveInstant(player2, 0, ghoul.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerRadCounters).isEmpty();
    }
}
