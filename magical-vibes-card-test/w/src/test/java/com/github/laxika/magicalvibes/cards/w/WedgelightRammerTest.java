package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({WedgelightRammer.class, GrizzlyBears.class})
class WedgelightRammerTest extends BaseCardTest {

    @Test
    @DisplayName("When Wedgelight Rammer enters, it creates a 2/2 Robot artifact creature token")
    void enteringCreatesRobotToken() {
        harness.setHand(player1, List.of(new WedgelightRammer()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().getColor()).isNull();
        assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(2);
    }

    @Test
    @DisplayName("Station puts counters equal to the tapped creature's power on Wedgelight Rammer")
    void stationUsesTappedCreaturePower() {
        Permanent rammer = harness.addToBattlefieldAndReturn(player1, new WedgelightRammer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(rammer), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(rammer.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("At nine charge counters, Wedgelight Rammer becomes a flying first-strike artifact creature")
    void nineCountersAnimateAndGrantKeywords() {
        Permanent rammer = harness.addToBattlefieldAndReturn(player1, new WedgelightRammer());

        rammer.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, rammer)).isFalse();
        assertThat(gqs.hasKeyword(gd, rammer, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, rammer, Keyword.FIRST_STRIKE)).isFalse();

        rammer.setCounterCount(CounterType.CHARGE, 9);
        assertThat(gqs.isCreature(gd, rammer)).isTrue();
        assertThat(gqs.getEffectivePower(gd, rammer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rammer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, rammer, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, rammer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Station requires another untapped creature")
    void stationNeedsAnotherUntappedCreature() {
        Permanent rammer = harness.addToBattlefieldAndReturn(player1, new WedgelightRammer());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(rammer), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick creature can station Wedgelight Rammer")
    void summoningSickCreatureCanStation() {
        Permanent rammer = harness.addToBattlefieldAndReturn(player1, new WedgelightRammer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(rammer), null, null);
        assertThat(bears.isTapped()).isTrue();
        assertThat(rammer.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();

        assertThat(rammer.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped creature cannot pay the station cost")
    void tappedCreatureCannotStation() {
        Permanent rammer = harness.addToBattlefieldAndReturn(player1, new WedgelightRammer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(rammer), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rammer.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("An opposing creature cannot pay the station cost")
    void opposingCreatureCannotStation() {
        Permanent rammer = harness.addToBattlefieldAndReturn(player1, new WedgelightRammer());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(rammer), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An animated Wedgelight Rammer cannot station itself")
    void animatedRammerCannotStationItself() {
        Permanent rammer = addCreatureReady(player1, new WedgelightRammer());
        rammer.setCounterCount(CounterType.CHARGE, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(rammer), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(rammer.isTapped()).isFalse();
        assertThat(rammer.getCounterCount(CounterType.CHARGE)).isEqualTo(9);
    }

    @Test
    @DisplayName("Dropping below nine charge counters removes animation and granted keywords")
    void losingCountersRemovesAnimationAndKeywords() {
        Permanent rammer = harness.addToBattlefieldAndReturn(player1, new WedgelightRammer());
        rammer.setCounterCount(CounterType.CHARGE, 10);
        assertThat(gqs.isCreature(gd, rammer)).isTrue();
        assertThat(gqs.hasKeyword(gd, rammer, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, rammer, Keyword.FIRST_STRIKE)).isTrue();

        rammer.setCounterCount(CounterType.CHARGE, 8);

        assertThat(gqs.isCreature(gd, rammer)).isFalse();
        assertThat(gqs.hasKeyword(gd, rammer, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, rammer, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(rammer.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    @DisplayName("Station cannot be activated outside a main phase")
    void stationRequiresMainPhase() {
        Permanent rammer = harness.addToBattlefieldAndReturn(player1, new WedgelightRammer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(rammer), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bears.isTapped()).isFalse();
    }
    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
