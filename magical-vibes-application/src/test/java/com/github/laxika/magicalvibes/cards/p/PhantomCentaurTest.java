package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CabalTrainee;
import com.github.laxika.magicalvibes.cards.c.Cagemail;
import com.github.laxika.magicalvibes.cards.f.FlaringPain;
import com.github.laxika.magicalvibes.cards.l.LavaDart;
import com.github.laxika.magicalvibes.cards.t.ToxicStench;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantomCentaur.class, CabalTrainee.class, Cagemail.class, FlaringPain.class,
        LavaDart.class, ToxicStench.class})
class PhantomCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new PhantomCentaur()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent centaur = findPermanent(player1, "Phantom Centaur");
        assertThat(centaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each damage event is prevented and removes one +1/+1 counter")
    void damageIsPreventedAndRemovesOneCounterPerEvent() {
        Permanent centaur = harness.enterBattlefieldAndReturn(player2, new PhantomCentaur());

        harness.setHand(player1, List.of(new LavaDart(), new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, centaur.getId());
        harness.castAndResolveInstant(player1, 0, centaur.getId());

        assertThat(findPermanent(player2, "Phantom Centaur")).isSameAs(centaur);
        assertThat(centaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(centaur.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Protection from black prevents black spells from targeting it")
    void protectionFromBlackPreventsTargeting() {
        Permanent centaur = harness.enterBattlefieldAndReturn(player2, new PhantomCentaur());

        harness.setHand(player1, List.of(new ToxicStench()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, centaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Protection from black prevents black creatures from blocking it")
    void protectionFromBlackPreventsBlocking() {
        Permanent centaur = harness.enterBattlefieldAndReturn(player1, new PhantomCentaur());
        centaur.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new CabalTrainee());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Damage prevention continues after all +1/+1 counters are removed")
    void preventsDamageWithoutCounters() {
        Permanent centaur = harness.enterBattlefieldAndReturn(player2, new PhantomCentaur());

        harness.setHand(player1, List.of(new Cagemail()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, centaur.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LavaDart(), new LavaDart(), new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, centaur.getId());
        harness.castAndResolveInstant(player1, 0, centaur.getId());
        harness.castAndResolveInstant(player1, 0, centaur.getId());

        assertThat(findPermanent(player2, "Phantom Centaur")).isSameAs(centaur);
        assertThat(centaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(centaur.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage prevention still removes a counter when damage cannot be prevented")
    void removesCounterWhenDamageCannotBePrevented() {
        Permanent centaur = harness.enterBattlefieldAndReturn(player2, new PhantomCentaur());

        harness.setHand(player1, List.of(new FlaringPain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player1, List.of(new LavaDart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, centaur.getId());

        assertThat(findPermanent(player2, "Phantom Centaur")).isSameAs(centaur);
        assertThat(centaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(centaur.getMarkedDamage()).isEqualTo(1);
    }
}
