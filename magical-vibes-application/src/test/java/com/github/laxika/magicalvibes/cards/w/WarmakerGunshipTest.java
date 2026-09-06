package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AtomicMicrosizer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarmakerGunship.class, AtomicMicrosizer.class, GrizzlyBears.class})
class WarmakerGunshipTest extends BaseCardTest {

    @Test
    @DisplayName("When Warmaker Gunship enters, it deals damage equal to the artifacts its controller controls")
    void enteringDealsDamageEqualToControlledArtifacts() {
        Permanent target = addOpponentBear();
        harness.addToBattlefield(player1, new AtomicMicrosizer());
        harness.addToBattlefield(player2, new AtomicMicrosizer());

        castWarmakerGunship(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Warmaker Gunship cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WarmakerGunship()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Station puts counters equal to the tapped creature's power on Warmaker Gunship")
    void stationUsesTappedCreaturePower() {
        Permanent gunship = harness.addToBattlefieldAndReturn(player1, new WarmakerGunship());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(gunship), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gunship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("At six charge counters, Warmaker Gunship becomes a flying artifact creature")
    void sixCountersAnimateAndGrantFlying() {
        Permanent gunship = harness.addToBattlefieldAndReturn(player1, new WarmakerGunship());

        gunship.setCounterCount(CounterType.CHARGE, 5);
        assertThat(gqs.isCreature(gd, gunship)).isFalse();
        assertThat(gqs.hasKeyword(gd, gunship, Keyword.FLYING)).isFalse();

        gunship.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, gunship)).isTrue();
        assertThat(gqs.hasKeyword(gd, gunship, Keyword.FLYING)).isTrue();

        gunship.setCounterCount(CounterType.CHARGE, 5);
        assertThat(gqs.isCreature(gd, gunship)).isFalse();
        assertThat(gqs.hasKeyword(gd, gunship, Keyword.FLYING)).isFalse();
    }

    private Permanent addOpponentBear() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(8);
        return harness.addToBattlefieldAndReturn(player2, bear);
    }

    private void castWarmakerGunship(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new WarmakerGunship()));
        addCastingMana();
        harness.castArtifact(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
