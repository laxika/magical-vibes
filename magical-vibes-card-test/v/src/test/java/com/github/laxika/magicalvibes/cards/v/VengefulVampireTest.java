package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DeathsCaress;
import com.github.laxika.magicalvibes.cards.f.FalkenrathTorturer;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VengefulVampire.class, Shock.class, LightningBolt.class, DeathsCaress.class,
        GrafdiggersCage.class, FalkenrathTorturer.class})
class VengefulVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Undying returns Vengeful Vampire with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VengefulVampire());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, vampire.getId());
        harness.passBothPriorities();

        Permanent returnedVampire = findPermanent(player1, "Vengeful Vampire");
        assertThat(returnedVampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Vengeful Vampire");
    }

    @Test
    @DisplayName("Undying does not return Vengeful Vampire when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VengefulVampire());
        vampire.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, vampire.getId());

        harness.assertNotOnBattlefield(player1, "Vengeful Vampire");
        harness.assertInGraveyard(player1, "Vengeful Vampire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second death after undying leaves the Vampire in its graveyard")
    void secondDeathDoesNotReturn() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VengefulVampire());
        harness.setHand(player1, List.of(new DeathsCaress(), new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 10);

        harness.castAndResolveSorcery(player1, 0, vampire.getId());
        harness.assertInGraveyard(player1, "Vengeful Vampire");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Vengeful Vampire");
        assertThat(returned.getId()).isNotEqualTo(vampire.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.castAndResolveSorcery(player1, 0, returned.getId());

        harness.assertNotOnBattlefield(player1, "Vengeful Vampire");
        harness.assertInGraveyard(player1, "Vengeful Vampire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Undying still triggers with a -1/-1 counter and returns without the old counter")
    void minusCounterDoesNotPreventUndying() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VengefulVampire());
        vampire.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, vampire.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Vengeful Vampire");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Vengeful Vampire");
    }

    @Test
    @DisplayName("Grafdigger's Cage prevents the undying return but not the trigger")
    void cagePreventsReturn() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VengefulVampire());
        harness.addToBattlefield(player2, new GrafdiggersCage());
        harness.setHand(player1, List.of(new DeathsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, vampire.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vengeful Vampire");
        harness.assertInGraveyard(player1, "Vengeful Vampire");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Vengeful Vampire")
    void groundCreatureCannotBlock() {
        addCreatureReady(player1, new VengefulVampire());
        addCreatureReady(player2, new FalkenrathTorturer());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Another flying creature can block Vengeful Vampire")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new VengefulVampire());
        Permanent blocker = addCreatureReady(player2, new VengefulVampire());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
