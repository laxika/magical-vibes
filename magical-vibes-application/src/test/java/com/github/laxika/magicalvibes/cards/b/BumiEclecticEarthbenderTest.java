package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BumiEclecticEarthbender.class, Forest.class, GrizzlyBears.class})
class BumiEclecticEarthbenderTest extends BaseCardTest {

    @Test
    void entersByEarthbendingALandYouControl() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        castBumi(land);

        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void attackingPutsTwoCountersOnEachLandCreatureYouControl() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent bumi = castBumi(land);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        bumi.setSummoningSick(false);

        declareAttackers(List.of(indexOf(player1, bumi)));
        resolveAllTriggers();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castBumi(Permanent targetLand) {
        harness.setHand(player1, List.of(new BumiEclecticEarthbender()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, 0, targetLand.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BumiEclecticEarthbender)
                .findFirst()
                .orElseThrow();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
