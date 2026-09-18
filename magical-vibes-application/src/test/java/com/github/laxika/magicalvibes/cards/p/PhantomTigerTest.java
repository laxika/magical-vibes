package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.h.HaplessResearcher;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.cards.l.LightningSurge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomTiger.class, LightningSurge.class, HaplessResearcher.class,
        LavaDart.class, FlaringPain.class})
class PhantomTigerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.castFromHand(player1, new PhantomTiger(), "{2}{G}");
        harness.passBothPriorities();

        Permanent tiger = findPermanent(player1, "Phantom Tiger");
        assertThat(tiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents noncombat damage and removes one +1/+1 counter")
    void preventsNoncombatDamageAndRemovesOneCounter() {
        Permanent tiger = addCreatureReady(player2, new PhantomTiger());
        tiger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new LightningSurge()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, tiger.getId());
        harness.passBothPriorities();

        assertThat(tiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(tiger.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tiger);
    }

    @Test
    @DisplayName("Prevents combat damage and removes one +1/+1 counter")
    void preventsCombatDamageAndRemovesOneCounter() {
        Permanent blocker = addCreatureReady(player2, new PhantomTiger());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        addCreatureReady(player1, new HaplessResearcher());
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents damage even with no +1/+1 counters")
    void preventsDamageWithoutCounters() {
        Permanent tiger = addCreatureReady(player2, new PhantomTiger());
        tiger.setToughnessModifier(1);

        castLavaDart(tiger);

        assertThat(tiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(tiger.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tiger);
    }

    @Test
    @DisplayName("Removing the last +1/+1 counter makes the 1/0 creature die")
    void diesWhenLastCounterIsRemoved() {
        Permanent tiger = addCreatureReady(player2, new PhantomTiger());
        tiger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castLavaDart(tiger);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tiger);
    }

    @Test
    @DisplayName("Unpreventable damage still removes one +1/+1 counter")
    void unpreventableDamageStillRemovesOneCounter() {
        Permanent tiger = addCreatureReady(player2, new PhantomTiger());
        tiger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new FlaringPain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        castLavaDart(tiger);

        assertThat(tiger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(tiger.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(tiger);
    }

    private void castLavaDart(Permanent target) {
        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
