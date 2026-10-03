package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlabasterWall;
import com.github.laxika.magicalvibes.cards.c.CharmedGriffin;
import com.github.laxika.magicalvibes.cards.c.CragSaurian;
import com.github.laxika.magicalvibes.cards.c.CateranSlaver;
import com.github.laxika.magicalvibes.cards.r.RamosianRally;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Brawl.class, AlabasterWall.class, CharmedGriffin.class, CragSaurian.class, CateranSlaver.class, RamosianRally.class})
class BrawlTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures gain the power damage ability until end of turn")
    void allCreaturesGainPowerDamageAbility() {
        addCreatureReady(player1, new CharmedGriffin());
        Permanent ownTarget = addCreatureReady(player1, new AlabasterWall());
        addCreatureReady(player2, new CharmedGriffin());
        Permanent opposingTarget = addCreatureReady(player2, new AlabasterWall());

        castBrawl();

        harness.activateAbility(player1, 0, null, opposingTarget.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, ownTarget.getId());
        harness.passBothPriorities();

        assertThat(ownTarget.getMarkedDamage()).isEqualTo(3);
        assertThat(opposingTarget.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The granted ability deals damage equal to the source creature's power and taps it")
    void dealsCurrentSourcePowerAndRequiresTap() {
        Permanent source = addCreatureReady(player1, new CragSaurian());
        Permanent target = addCreatureReady(player2, new CateranSlaver());

        castBrawl();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures that enter after Brawl resolves do not gain the ability")
    void laterCreaturesDoNotGainAbility() {
        addCreatureReady(player1, new CharmedGriffin());
        castBrawl();

        Permanent laterCreature = addCreatureReady(player1, new CharmedGriffin());
        Permanent opposingCreature = addCreatureReady(player2, new AlabasterWall());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(laterCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The granted ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        addCreatureReady(player1, new CharmedGriffin());
        Permanent target = addCreatureReady(player2, new AlabasterWall());
        castBrawl();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability can target only creatures")
    void grantedAbilityCannotTargetPlayer() {
        addCreatureReady(player1, new CharmedGriffin());
        castBrawl();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted tap ability cannot be activated by a summoning-sick creature")
    void summoningSicknessPreventsActivation() {
        Permanent source = addCreatureReady(player1, new CharmedGriffin());
        source.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new AlabasterWall());
        castBrawl();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A creature can target itself with the granted ability")
    void creatureCanDamageItself() {
        Permanent source = addCreatureReady(player1, new CharmedGriffin());
        castBrawl();

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Charmed Griffin");
        harness.assertInGraveyard(player1, "Charmed Griffin");
    }

    @Test
    @DisplayName("Damage uses the creature's power at resolution rather than activation")
    void powerIsEvaluatedAtResolution() {
        addCreatureReady(player1, new CharmedGriffin());
        Permanent target = addCreatureReady(player2, new CateranSlaver());
        castBrawl();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castFromHand(player1, new RamosianRally(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Cateran Slaver");
    }

    @Test
    @DisplayName("The granted ability still deals damage after its source dies")
    void abilityResolvesAfterSourceDies() {
        Permanent source = addCreatureReady(player1, new CharmedGriffin());
        Permanent target = addCreatureReady(player2, new AlabasterWall());
        addCreatureReady(player2, new CharmedGriffin());
        castBrawl();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player2, 1, null, source.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Charmed Griffin");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    private void castBrawl() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Brawl(), "{3}{R}{R}");
        harness.passBothPriorities();
    }
}
