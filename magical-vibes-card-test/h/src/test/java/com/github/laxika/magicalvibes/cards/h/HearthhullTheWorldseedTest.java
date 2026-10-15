package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({HearthhullTheWorldseed.class, Forest.class, GrizzlyBears.class})
class HearthhullTheWorldseedTest extends BaseCardTest {

    @Test
    @DisplayName("Station adds charge counters equal to the tapped creature's power")
    void stationUsesAnotherCreaturePower() {
        Permanent hearthhull = harness.addToBattlefieldAndReturn(player1, new HearthhullTheWorldseed());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(hearthhull), 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(hearthhull.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("At eight charge counters, Hearthhull becomes a creature with flying, vigilance, and haste")
    void eightCountersAnimateHearthhull() {
        Permanent hearthhull = harness.addToBattlefieldAndReturn(player1, new HearthhullTheWorldseed());

        hearthhull.setCounterCount(CounterType.CHARGE, 7);
        assertThat(gqs.isCreature(gd, hearthhull)).isFalse();

        hearthhull.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, hearthhull)).isTrue();
        assertThat(gqs.hasKeyword(gd, hearthhull, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, hearthhull, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, hearthhull, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The charge-gated ability sacrifices a land, draws two cards, and grants an extra land play")
    void sacrificeLandDrawsAndGrantsLandPlay() {
        Permanent hearthhull = harness.addToBattlefieldAndReturn(player1, new HearthhullTheWorldseed());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        hearthhull.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(hearthhull), 1, null, null);
        harness.handlePermanentChosen(player1, land.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Forest");

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
    }

    @Test
    @DisplayName("The draw ability requires at least two charge counters")
    void drawAbilityRequiresTwoChargeCounters() {
        Permanent hearthhull = harness.addToBattlefieldAndReturn(player1, new HearthhullTheWorldseed());
        harness.addToBattlefield(player1, new Forest());
        hearthhull.setCounterCount(CounterType.CHARGE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(hearthhull), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("charge counters");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
