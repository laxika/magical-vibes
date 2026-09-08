package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({PinnacleKillShip.class, GrizzlyBears.class})
class PinnacleKillShipTest extends BaseCardTest {

    @Test
    @DisplayName("When Pinnacle Kill-Ship enters, it deals 10 damage to a target creature")
    void enteringDealsTenDamageToTargetCreature() {
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setToughness(20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        harness.setHand(player1, List.of(new PinnacleKillShip()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(10);
    }

    @Test
    @DisplayName("Pinnacle Kill-Ship can enter without choosing a creature")
    void enteringCanDeclineTarget() {
        harness.setHand(player1, List.of(new PinnacleKillShip()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Pinnacle Kill-Ship")).isNotNull();
    }

    @Test
    @DisplayName("Station puts counters equal to the tapped creature's power on Pinnacle Kill-Ship")
    void stationUsesTappedCreaturePower() {
        Permanent ship = harness.addToBattlefieldAndReturn(player1, new PinnacleKillShip());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(ship), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(ship.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("At seven charge counters, Pinnacle Kill-Ship becomes a flying artifact creature")
    void sevenCountersAnimateAndGrantFlying() {
        Permanent ship = harness.addToBattlefieldAndReturn(player1, new PinnacleKillShip());

        ship.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, ship)).isFalse();
        assertThat(gqs.hasKeyword(gd, ship, Keyword.FLYING)).isFalse();

        ship.setCounterCount(CounterType.CHARGE, 7);
        assertThat(gqs.isCreature(gd, ship)).isTrue();
        assertThat(gqs.hasKeyword(gd, ship, Keyword.FLYING)).isTrue();

        ship.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, ship)).isFalse();
        assertThat(gqs.hasKeyword(gd, ship, Keyword.FLYING)).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
