package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SterlingHound;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CongregationGryff.class, SterlingHound.class})
class CongregationGryffTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 3 taps creatures with total power 3 and saddles Congregation Gryff")
    void saddleThreeTapsAThreePowerCreature() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        Permanent saddler = addCreatureReady(player1, new SterlingHound());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gryff.isSaddled()).isTrue();
        assertThat(saddler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Saddled attack boosts Congregation Gryff by the number of Mounts controlled")
    void saddledAttackScalesWithControlledMounts() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        addCreatureReady(player1, new CongregationGryff());
        gryff.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, gryff)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gryff)).isEqualTo(6);
    }

    @Test
    @DisplayName("Unsaddled attack does not boost Congregation Gryff")
    void unsaddledAttackDoesNotBoost() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, gryff)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gryff)).isEqualTo(4);
    }

    @Test
    void saddleCanTapSeveralCreaturesWithSummoningSickness() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CongregationGryff());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CongregationGryff());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new CongregationGryff());

        harness.activateAbility(player1, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gryff.isTapped()).isFalse();
        assertThat(gryff.isSaddled()).isFalse();

        resolveAllTriggers();

        assertThat(gryff.isSaddled()).isTrue();
    }

    @Test
    void saddleCannotUseItsOwnPowerOrOpponentsCreatures() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        addCreatureReady(player1, new CongregationGryff());
        addCreatureReady(player1, new CongregationGryff());
        addCreatureReady(player2, new SterlingHound());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gryff.isSaddled()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(p -> !p.isTapped());
    }

    @Test
    void tappedCreaturesCannotPaySaddleCost() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        Permanent saddler = addCreatureReady(player1, new SterlingHound());
        saddler.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gryff.isSaddled()).isFalse();
    }

    @Test
    void saddleCannotBeActivatedDuringCombat() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        Permanent saddler = addCreatureReady(player1, new SterlingHound());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gryff.isSaddled()).isFalse();
        assertThat(saddler.isTapped()).isFalse();
    }

    @Test
    void saddleCannotBeActivatedDuringOpponentsMainPhase() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        Permanent saddler = addCreatureReady(player1, new SterlingHound());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gryff.isSaddled()).isFalse();
        assertThat(saddler.isTapped()).isFalse();
    }

    @Test
    void saddleCannotBeActivatedWhileAnotherAbilityIsOnTheStack() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        Permanent saddler = addCreatureReady(player1, new SterlingHound());
        harness.activateAbility(player1, 0, null, null);
        Permanent secondSaddler = addCreatureReady(player1, new SterlingHound());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(saddler.isTapped()).isTrue();
        assertThat(secondSaddler.isTapped()).isFalse();
        resolveAllTriggers();
        assertThat(gryff.isSaddled()).isTrue();
    }

    @Test
    void groundCreatureCannotBlockGryff() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        gryff.setAttacking(true);
        addCreatureReady(player2, new SterlingHound());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void boostIncludesItselfButExcludesOpposingMountsAndNonMounts() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        addCreatureReady(player1, new SterlingHound());
        addCreatureReady(player2, new CongregationGryff());
        gryff.setSaddled(true);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, gryff)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gryff)).isEqualTo(5);
    }

    @Test
    void mountCountIsDeterminedOnResolutionAndBoostThenStaysFixed() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        Permanent otherMount = addCreatureReady(player1, new CongregationGryff());
        gryff.setSaddled(true);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(otherMount);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, gryff)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gryff)).isEqualTo(5);

        addCreatureReady(player1, new CongregationGryff());

        assertThat(gqs.getEffectivePower(gd, gryff)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gryff)).isEqualTo(5);
    }

    @Test
    void saddledAttackDealsBoostedDamageAndGainsLife() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        addCreatureReady(player1, new CongregationGryff());
        gryff.setSaddled(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void saddleAndAttackBoostExpireAtEndOfTurn() {
        Permanent gryff = addCreatureReady(player1, new CongregationGryff());
        addCreatureReady(player1, new SterlingHound());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, gryff)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gryff)).isEqualTo(5);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gryff.isSaddled()).isFalse();
        assertThat(gqs.getEffectivePower(gd, gryff)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gryff)).isEqualTo(4);
    }
}
