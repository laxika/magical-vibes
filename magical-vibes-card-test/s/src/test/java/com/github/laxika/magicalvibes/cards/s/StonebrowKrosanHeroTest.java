package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StonebrowKrosanHero.class, GrizzlyBears.class, HavenwoodWurm.class})
class StonebrowKrosanHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking Stonebrow gets +2/+2")
    void attackingStonebrowIsBoosted() {
        Permanent stonebrow = addCreatureReady(player1, new StonebrowKrosanHero());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, stonebrow)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, stonebrow)).isEqualTo(6);
    }

    @Test
    @DisplayName("A creature without trample does not trigger Stonebrow's ability")
    void nonTramplingAttackerIsNotBoosted() {
        addCreatureReady(player1, new StonebrowKrosanHero());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Stonebrow's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent stonebrow = addCreatureReady(player1, new StonebrowKrosanHero());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, stonebrow)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stonebrow)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stonebrow)).isEqualTo(4);
    }

    @Test
    @DisplayName("A trampling attacker is boosted while Stonebrow stays back")
    void otherTramplingAttackerIsBoosted() {
        Permanent stonebrow = addCreatureReady(player1, new StonebrowKrosanHero());
        Permanent wurm = addCreatureReady(player1, new HavenwoodWurm());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, stonebrow)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, stonebrow)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each trampling attacker gets its own boost")
    void multipleTramplingAttackersAreBoostedIndividually() {
        Permanent stonebrow = addCreatureReady(player1, new StonebrowKrosanHero());
        Permanent wurm = addCreatureReady(player1, new HavenwoodWurm());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, stonebrow)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, stonebrow)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(8);
    }

    @Test
    @DisplayName("An opposing trampling attacker does not trigger Stonebrow")
    void opposingTramplingAttackerIsNotBoosted() {
        addCreatureReady(player1, new StonebrowKrosanHero());
        Permanent wurm = addCreatureReady(player2, new HavenwoodWurm());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(6);
    }

    @Test
    @DisplayName("A pending boost still resolves after Stonebrow leaves the battlefield")
    void boostResolvesAfterSourceLeaves() {
        Permanent stonebrow = addCreatureReady(player1, new StonebrowKrosanHero());
        Permanent wurm = addCreatureReady(player1, new HavenwoodWurm());

        declareAttackers(player1, List.of(1));
        gd.playerBattlefields.get(player1.getId()).remove(stonebrow);
        gd.playerGraveyards.get(player1.getId()).add(stonebrow.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, wurm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, wurm)).isEqualTo(8);
    }
}
