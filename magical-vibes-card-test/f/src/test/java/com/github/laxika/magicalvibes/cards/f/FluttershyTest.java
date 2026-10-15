package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fluttershy.class, GrizzlyBears.class, LlanowarElves.class})
class FluttershyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on the target player's tailed creature when both targets are chosen")
    void resolvesBothTargetGroups() {
        Permanent fluttershy = addCreatureReady(player1, new Fluttershy());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());

        activate(fluttershy, List.of(player2.getId(), targetCreature.getId()));

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, targetCreature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The creature target may be omitted")
    void mayOmitCreatureTarget() {
        Permanent fluttershy = addCreatureReady(player1, new Fluttershy());
        Permanent targetCreature = addCreatureReady(player2, new GrizzlyBears());

        activate(fluttershy, List.of(player2.getId()));

        assertThat(gqs.getEffectivePower(gd, targetCreature)).isEqualTo(3);
        assertThat(targetCreature.isCantAttackThisTurn()).isFalse();
        assertThat(targetCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Only creatures with tails receive counters")
    void doesNotPutCounterOnCreatureWithoutTail() {
        Permanent fluttershy = addCreatureReady(player1, new Fluttershy());
        Permanent tailedCreature = addCreatureReady(player2, new Fluttershy());
        Permanent taillessCreature = addCreatureReady(player2, new LlanowarElves());

        activate(fluttershy, List.of(player2.getId()));

        assertThat(tailedCreature.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(taillessCreature.getPlusOnePlusOneCounters()).isZero();
        assertThat(fluttershy.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("The controller may target themselves and counters are put on every tailed creature")
    void mayTargetControllerAndCounterMultipleCreatures() {
        Permanent fluttershy = addCreatureReady(player1, new Fluttershy());
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());

        activate(fluttershy, List.of(player1.getId(), opposingBear.getId()));

        assertThat(fluttershy.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(firstBear.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(secondBear.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(opposingBear.getPlusOnePlusOneCounters()).isZero();
        assertThat(fluttershy.isTapped()).isTrue();
    }

    private void activate(Permanent fluttershy, List<java.util.UUID> targets) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(fluttershy),
                0,
                targets);
        harness.passBothPriorities();
    }
}
