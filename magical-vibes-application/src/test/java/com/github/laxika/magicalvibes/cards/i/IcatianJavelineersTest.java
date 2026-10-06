package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FlyingMen;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcatianJavelineers.class, FlyingMen.class, TormodsCrypt.class})
class IcatianJavelineersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a javelin counter")
    void entersWithJavelinCounter() {
        harness.castFromHand(player1, new IcatianJavelineers(), "{W}");
        harness.passBothPriorities();

        Permanent javelineers = findPermanent(player1, "Icatian Javelineers");
        assertThat(javelineers.getCounterCount(CounterType.JAVELIN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to a target player and removes its javelin counter")
    void dealsDamageAndRemovesCounter() {
        harness.setLife(player2, 20);
        Permanent javelineers = addReadyJavelineers();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(javelineers.isTapped()).isTrue();
        assertThat(javelineers.getCounterCount(CounterType.JAVELIN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void dealsDamageToTargetCreature() {
        addReadyJavelineers();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FlyingMen());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Flying Men");
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent javelineers = addReadyJavelineers();

        harness.activateAbility(player1, 0, null, javelineers.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Icatian Javelineers");
    }

    @Test
    @DisplayName("Cannot activate without a javelin counter")
    void cannotActivateWithoutJavelinCounter() {
        Permanent javelineers = addReadyJavelineers();
        javelineers.setCounterCount(CounterType.JAVELIN, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent javelineers = harness.enterBattlefieldAndReturn(player1, new IcatianJavelineers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(javelineers.isTapped()).isFalse();
        assertThat(javelineers.getCounterCount(CounterType.JAVELIN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        Permanent javelineers = addReadyJavelineers();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new TormodsCrypt());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(javelineers.isTapped()).isFalse();
        assertThat(javelineers.getCounterCount(CounterType.JAVELIN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate while tapped even with a javelin counter")
    void cannotActivateWhileTapped() {
        Permanent javelineers = addReadyJavelineers();
        javelineers.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(javelineers.getCounterCount(CounterType.JAVELIN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Untapping does not allow a second activation after the counter is spent")
    void cannotReuseSpentJavelin() {
        Permanent javelineers = addReadyJavelineers();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        javelineers.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(javelineers.isTapped()).isFalse();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The ability still deals damage after its source dies in response")
    void abilityResolvesAfterSourceDies() {
        Permanent javelineers = addReadyJavelineers();
        Permanent opponentJavelineers = harness.enterBattlefieldAndReturn(player2, new IcatianJavelineers());
        opponentJavelineers.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player2, 0, null, javelineers.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Icatian Javelineers");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    private Permanent addReadyJavelineers() {
        Permanent javelineers = harness.enterBattlefieldAndReturn(player1, new IcatianJavelineers());
        javelineers.setSummoningSick(false);
        return javelineers;
    }
}
