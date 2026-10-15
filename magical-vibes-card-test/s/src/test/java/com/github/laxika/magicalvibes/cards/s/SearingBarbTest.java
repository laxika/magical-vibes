package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FurnaceHostCharger;
import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearingBarb.class, Forest.class, FurnaceHostCharger.class,
        ChandraHopesBeacon.class, InvasionOfZendikar.class})
class SearingBarbTest extends BaseCardTest {

    @Test
    void damagesCreaturePreventsBlockingAndIncubates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        cast(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(target.isCantBlockThisTurn()).isTrue();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void damagesPlayerAndIncubatesWithoutCreatureRestriction() {
        harness.setLife(player2, 20);
        cast(player2.getId());

        harness.assertLife(player2, 18);
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SearingBarb()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("any target");
    }

    @Test
    void damagesPlaneswalkerAndIncubates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        target.setCounterCount(CounterType.LOYALTY, 5);
        cast(target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
    }

    @Test
    void damagesBattleAndIncubates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        target.setCounterCount(CounterType.DEFENSE, 3);
        target.setProtectorPlayerId(player1.getId());
        cast(target.getId());

        assertThat(target.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
    }

    @Test
    void preventedDamageStillPreventsCreatureFromBlockingAndIncubates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        target.setCounterCount(CounterType.SHIELD, 1);
        cast(target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
    }

    @Test
    void illegalSoleTargetPreventsIncubation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceHostCharger());
        putSpellOnStack(target.getId());
        target.setCounterCount(CounterType.HEXPROOF, 1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(countPermanents(player1, "Incubator")).isZero();
        harness.assertInGraveyard(player1, "Searing Barb");
    }

    @Test
    void canTargetControllerAndCreatesExactlyOneIncubator() {
        harness.setLife(player1, 20);
        cast(player1.getId());

        harness.assertLife(player1, 18);
        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
        assertThat(countPermanents(player2, "Incubator")).isZero();
    }

    @Test
    void incubatorTransformsIntoOneOneCreatureWithItsCounterRetained() {
        cast(player2.getId());
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(gqs.isCreature(gd, incubator)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(1);
    }

    private void cast(java.util.UUID targetId) {
        putSpellOnStack(targetId);
        harness.passBothPriorities();
    }

    private void putSpellOnStack(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new SearingBarb()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetId);
    }
}
