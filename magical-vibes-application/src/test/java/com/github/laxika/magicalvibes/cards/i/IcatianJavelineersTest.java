package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.Aeolipile;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcatianJavelineers.class, IcatianInfantry.class, Aeolipile.class})
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IcatianInfantry());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Icatian Infantry");
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
    @DisplayName("Cannot target a noncreature artifact")
    void cannotTargetNoncreatureArtifact() {
        Permanent javelineers = addReadyJavelineers();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Aeolipile());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(javelineers.isTapped()).isFalse();
        assertThat(javelineers.getCounterCount(CounterType.JAVELIN)).isEqualTo(1);
    }

    private Permanent addReadyJavelineers() {
        Permanent javelineers = addCreatureReady(player1, new IcatianJavelineers());
        javelineers.setCounterCount(CounterType.JAVELIN, 1);
        return javelineers;
    }
}
