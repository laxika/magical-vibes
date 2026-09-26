package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KavuAggressor.class)
class KavuAggressorTest extends BaseCardTest {

    @Test
    void entersWithoutCounterWhenNotKicked() {
        harness.castFromHand(player1, new KavuAggressor(), "{2}{R}");
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Kavu Aggressor");
        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithOneCounterWhenKicked() {
        harness.setHand(player1, List.of(new KavuAggressor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Kavu Aggressor");
        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotBeDeclaredAsBlocker() {
        Permanent blocker = addCreatureReady(player2, new KavuAggressor());
        Permanent attacker = addCreatureReady(player1, new KavuAggressor());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }
}
