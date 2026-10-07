package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlhammarretsArchive;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreeOfPerdition.class, TaintedRemedy.class, AlhammarretsArchive.class, OneWithTheStars.class})
class TreeOfPerditionTest extends BaseCardTest {

    @Test
    @DisplayName("Exchange sets opponent life to toughness and toughness to old life total")
    void exchangeOpponentLifeAndToughness() {
        Permanent tree = addReadyTree(player1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);
    }

    @Test
    @DisplayName("Exchange when opponent life is lower than toughness raises opponent life")
    void exchangeWhenOpponentLifeLowerThanToughness() {
        Permanent tree = addReadyTree(player1);
        harness.setLife(player2, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(5);
    }

    @Test
    @DisplayName("Toughness override persists across turns")
    void toughnessPersistsAcrossTurns() {
        Permanent tree = addReadyTree(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);

        tree.resetModifiers();
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);
    }

    @Test
    @DisplayName("+1/+1 counters apply on top of exchanged toughness")
    void countersApplyOnTopOfExchangedToughness() {
        Permanent tree = addReadyTree(player1);
        tree.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(22);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        addReadyTree(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Activating ability taps Tree of Perdition")
    void activatingTapsTree() {
        Permanent tree = addReadyTree(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(tree.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent tree = harness.addToBattlefieldAndReturn(player1, new TreeOfPerdition());
        tree.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTree(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TreeOfPerdition());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    void equalValuesStillSetBaseToughnessBeforeCounters() {
        Permanent tree = addReadyTree(player1);
        tree.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player2, 15);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(17);
    }

    @Test
    void lifeGainIsReplacedByTaintedRemedy() {
        Permanent tree = addReadyTree(player1);
        harness.addToBattlefield(player1, new TaintedRemedy());
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(10);
    }

    @Test
    void lifeGainIsDoubledByAlhammarretsArchive() {
        Permanent tree = addReadyTree(player1);
        harness.addToBattlefield(player2, new AlhammarretsArchive());
        harness.setLife(player2, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(21);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(5);
    }

    @Test
    void exchangedLifeGainCountsTowardLifeGainedThisTurn() {
        addReadyTree(player1);
        harness.setLife(player2, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(13);
        assertThat(gd.getLifeGainedThisTurn(player2.getId())).isEqualTo(8);
    }

    @Test
    void exchangeDoesNotOccurAfterTreeLeavesBattlefield() {
        Permanent tree = addReadyTree(player1);
        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(tree);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void exchangeDoesNotOccurWhenTreeIsNoLongerCreature() {
        Permanent tree = addReadyTree(player1);
        harness.activateAbility(player1, 0, null, player2.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new OneWithTheStars());
        aura.setAttachedTo(tree.getId());
        assertThat(gqs.isCreature(gd, tree)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void exchangeUsesToughnessAndLifeAtResolution() {
        Permanent tree = addReadyTree(player1);
        harness.activateAbility(player1, 0, null, player2.getId());
        tree.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player2, 7);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(9);
    }

    @Test
    void cannotExchangeWhenOpponentCannotLoseLife() {
        Permanent tree = addReadyTree(player1);
        gd.playersWhoCantLoseLifeThisTurn.add(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(13);
    }

    @Test
    void cannotExchangeWhenOpponentCannotGainLife() {
        Permanent tree = addReadyTree(player1);
        harness.setLife(player2, 5);
        gd.playersWhoCantGainLifeThisTurn.add(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(13);
    }
}
