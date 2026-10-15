package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.l.LlanowarWastes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WidowsBite.class, CrawWurm.class, LlanowarWastes.class})
class WidowsBiteTest extends BaseCardTest {

    @Test
    void deathtouchModeGrantsDeathtouchUntilEndOfTurn() {
        Permanent target = addCreatureReady(player2, new CrawWurm());

        cast(new int[]{0}, List.of(target.getId()), List.of());

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void minusTwoModeAppliesMinusTwoMinusTwoUntilEndOfTurn() {
        Permanent target = addCreatureReady(player2, new CrawWurm());

        cast(new int[]{1}, List.of(target.getId()), List.of());

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void teamworkAllowsBothModesAndTheSameCreatureMayBeTargetedTwice() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new CrawWurm());

        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()), List.of(teammate.getId()));

        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        assertThat(teammate.isTapped()).isTrue();
    }

    @Test
    void bothModesCannotBeChosenWithoutTeamwork() {
        Permanent target = addCreatureReady(player2, new CrawWurm());

        assertThatThrownBy(() -> {
            cast(new int[]{0, 1}, List.of(target.getId(), target.getId()), List.of());
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void modesCannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new LlanowarWastes());

        assertThatThrownBy(() -> {
            cast(new int[]{0}, List.of(land.getId()), List.of());
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void teamworkRequiresBothModesRatherThanOnlyDeathtouch() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new CrawWurm());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(target.getId()), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void teamworkRequiresBothModesRatherThanOnlyMinusTwo() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new CrawWurm());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(target.getId()), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void teamworkModesCanTargetDifferentCreaturesAndExpireAtCleanup() {
        Permanent deathtouchTarget = addCreatureReady(player1, new CrawWurm());
        Permanent reducedTarget = addCreatureReady(player2, new CrawWurm());

        cast(new int[]{0, 1}, List.of(deathtouchTarget.getId(), reducedTarget.getId()),
                List.of(deathtouchTarget.getId()));

        assertThat(gqs.hasKeyword(gd, deathtouchTarget, Keyword.DEATHTOUCH)).isTrue();
        assertThat(deathtouchTarget.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, reducedTarget, Keyword.DEATHTOUCH)).isFalse();
        assertThat(reducedTarget.getPowerModifier()).isEqualTo(-2);
        assertThat(reducedTarget.getToughnessModifier()).isEqualTo(-2);
        assertThat(deathtouchTarget.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, deathtouchTarget, Keyword.DEATHTOUCH)).isFalse();
        assertThat(reducedTarget.getPowerModifier()).isZero();
        assertThat(reducedTarget.getToughnessModifier()).isZero();
    }

    @Test
    void teamworkRejectsInsufficientTotalPower() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new CrawWurm());
        teammate.setPowerModifier(-4);

        assertThatThrownBy(() -> cast(new int[]{0, 1}, List.of(target.getId(), target.getId()),
                List.of(teammate.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(teammate.isTapped()).isFalse();
    }

    @Test
    void teamworkAcceptsExactlyThreePowerAndSummoningSickCreatures() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        teammate.setSummoningSick(true);
        teammate.setPowerModifier(-3);

        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()), List.of(teammate.getId()));

        assertThat(teammate.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    void minusTwoModeKillsACreatureWithTwoRemainingToughness() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        target.setToughnessModifier(-2);

        cast(new int[]{1}, List.of(target.getId()), List.of());

        harness.assertNotOnBattlefield(player2, "Craw Wurm");
        harness.assertInGraveyard(player2, "Craw Wurm");
    }

    @Test
    void teamworkRejectsTappedCreatures() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        Permanent teammate = addCreatureReady(player1, new CrawWurm());
        teammate.tap();

        assertThatThrownBy(() -> cast(new int[]{0, 1}, List.of(target.getId(), target.getId()),
                List.of(teammate.getId()))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void teamworkRejectsAnOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new CrawWurm());

        assertThatThrownBy(() -> cast(new int[]{0, 1}, List.of(target.getId(), target.getId()),
                List.of(target.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isFalse();
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds, List<java.util.UUID> teamworkIds) {
        harness.setHand(player1, List.of(new WidowsBite()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        if (teamworkIds.isEmpty()) {
            harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        } else {
            harness.getGameService().playCard(gd, player1, 0,
                    ChooseOneEffect.encodeModeSelection(1, 2, modes),
                    null, null, targetIds, List.of(), false, null, null, null, null, null,
                    false, null, null, null, teamworkIds);
        }
        harness.passBothPriorities();
    }
}
