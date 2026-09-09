package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.l.LlanowarWastes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

    private void cast(int[] modes, List<java.util.UUID> targetIds, List<java.util.UUID> teamworkIds) {
        harness.setHand(player1, List.of(new WidowsBite()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.getGameService().playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, modes),
                null, null, targetIds, List.of(), false, null, null, null, null, null,
                false, null, null, null, teamworkIds);
        harness.passBothPriorities();
    }
}
