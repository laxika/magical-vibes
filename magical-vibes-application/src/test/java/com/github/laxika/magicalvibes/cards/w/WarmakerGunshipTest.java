package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AtomicMicrosizer;
import com.github.laxika.magicalvibes.cards.g.GravpackMonoist;
import com.github.laxika.magicalvibes.cards.t.TapestryWarden;
import com.github.laxika.magicalvibes.cards.t.TerritorialBruntar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarmakerGunship.class, AtomicMicrosizer.class, GravpackMonoist.class, TerritorialBruntar.class, TapestryWarden.class})
class WarmakerGunshipTest extends BaseCardTest {

    @Test
    @DisplayName("When Warmaker Gunship enters, it deals damage equal to the artifacts its controller controls")
    void enteringDealsDamageEqualToControlledArtifacts() {
        Permanent target = addOpponentCreature();
        harness.addToBattlefield(player1, new AtomicMicrosizer());
        harness.addToBattlefield(player2, new AtomicMicrosizer());

        castWarmakerGunship(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Warmaker Gunship cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GravpackMonoist());
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
        Permanent creature = addCreatureReady(player1, new GravpackMonoist());

        harness.activateAbility(player1, battlefieldIndex(gunship), null, null);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
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

    @Test
    void enteringCountsArtifactsAtResolution() {
        Permanent target = addOpponentCreature();
        harness.setHand(player1, List.of(new WarmakerGunship()));
        addCastingMana();
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new AtomicMicrosizer());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canEnterWithoutAnOpposingCreature() {
        harness.castFromHand(player1, new WarmakerGunship(), "{2}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Warmaker Gunship");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void stationCanTapASummoningSickCreatureAndContinueAboveThreshold() {
        Permanent gunship = harness.addToBattlefieldAndReturn(player1, new WarmakerGunship());
        gunship.setCounterCount(CounterType.CHARGE, 6);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TerritorialBruntar());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(gunship), null, null);
        assertThat(creature.isTapped()).isTrue();
        assertThat(gunship.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
        resolveAllTriggers();

        assertThat(gunship.getCounterCount(CounterType.CHARGE)).isEqualTo(12);
        assertThat(gqs.hasKeyword(gd, gunship, Keyword.FLYING)).isTrue();
    }

    @Test
    void stationCannotTapTheGunshipItselfOrAnOpposingCreature() {
        Permanent gunship = harness.addToBattlefieldAndReturn(player1, new WarmakerGunship());
        gunship.setCounterCount(CounterType.CHARGE, 6);
        harness.addToBattlefield(player2, new TerritorialBruntar());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(gunship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gunship.isTapped()).isFalse();
        assertThat(gunship.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    void stationCannotTapAnAlreadyTappedCreature() {
        Permanent gunship = harness.addToBattlefieldAndReturn(player1, new WarmakerGunship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TerritorialBruntar());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(gunship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gunship.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void stationCannotBeActivatedDuringCombat() {
        Permanent gunship = harness.addToBattlefieldAndReturn(player1, new WarmakerGunship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TerritorialBruntar());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(gunship), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void stationUsesLastKnownPowerWhenTappedCreatureDies() {
        Permanent gunship = harness.addToBattlefieldAndReturn(player1, new WarmakerGunship());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TerritorialBruntar());

        harness.activateAbility(player1, battlefieldIndex(gunship), null, null);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setMarkedDamage(7);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Territorial Bruntar");
        assertThat(gunship.getCounterCount(CounterType.CHARGE)).isEqualTo(7);
    }

    @Test
    void stationUsesToughnessWithTapestryWarden() {
        Permanent gunship = harness.addToBattlefieldAndReturn(player1, new WarmakerGunship());
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new TapestryWarden());

        harness.activateAbility(player1, battlefieldIndex(gunship), null, null);
        resolveAllTriggers();

        assertThat(warden.isTapped()).isTrue();
        assertThat(gunship.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    void negativePowerDoesNotRemoveChargeCounters() {
        Permanent gunship = harness.addToBattlefieldAndReturn(player1, new WarmakerGunship());
        gunship.setCounterCount(CounterType.CHARGE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GravpackMonoist());
        creature.setPowerModifier(-3);

        harness.activateAbility(player1, battlefieldIndex(gunship), null, null);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gunship.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    private Permanent addOpponentCreature() {
        return harness.addToBattlefieldAndReturn(player2, new TerritorialBruntar());
    }

    private void castWarmakerGunship(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new WarmakerGunship()));
        addCastingMana();
        harness.castArtifact(player1, 0, targetId);
        resolveAllTriggers();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
