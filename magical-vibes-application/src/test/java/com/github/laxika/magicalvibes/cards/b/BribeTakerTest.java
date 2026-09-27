package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BribeTaker.class, GrizzlyBears.class})
class BribeTakerTest extends BaseCardTest {

    @Test
    void putsChosenCounterForEachControlledCounterKind() {
        Permanent chargePermanent = addCreatureReady(player1, new GrizzlyBears());
        Permanent plusOnePermanent = addCreatureReady(player1, new GrizzlyBears());
        chargePermanent.setCounterCount(CounterType.CHARGE, 1);
        plusOnePermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castBribeTaker();
        harness.passBothPriorities();
        Permanent bribeTaker = bribeTakerOnBattlefield();
        harness.passBothPriorities();

        harness.handleListChoice(player1, "charge counters");
        harness.handleListChoice(player1, "+1/+1 counters");

        assertThat(bribeTaker.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(bribeTaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayDeclineEachCounterKind() {
        Permanent support = addCreatureReady(player1, new GrizzlyBears());
        support.setCounterCount(CounterType.CHARGE, 1);

        castBribeTaker();
        harness.passBothPriorities();
        Permanent bribeTaker = bribeTakerOnBattlefield();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "Don't put a counter");

        assertThat(bribeTaker.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(bribeTaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsOnlyPermanentsControlledByTheSourceController() {
        Permanent opponentPermanent = addCreatureReady(player2, new GrizzlyBears());
        opponentPermanent.setCounterCount(CounterType.CHARGE, 1);

        castBribeTaker();
        harness.passBothPriorities();
        Permanent bribeTaker = bribeTakerOnBattlefield();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bribeTaker.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(bribeTaker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castBribeTaker() {
        harness.setHand(player1, List.of(new BribeTaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
    }

    private Permanent bribeTakerOnBattlefield() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Bribe Taker"))
                .findFirst()
                .orElseThrow();
    }
}
