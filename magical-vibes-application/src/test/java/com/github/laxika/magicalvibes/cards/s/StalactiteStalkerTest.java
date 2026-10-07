package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CogworkWrestler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StalactiteStalker.class, Forest.class, GrizzlyBears.class, ZuranOrb.class, CogworkWrestler.class})
class StalactiteStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself at your end step after descending")
    void putsCounterAfterDescending() {
        Permanent stalker = addStalker();
        sacrificeForestToDescend();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on itself at your end step without descending")
    void doesNotPutCounterWithoutDescending() {
        Permanent stalker = addStalker();

        advanceToEndStep(player1);

        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice ability gives a creature -X/-X using the stalker's power")
    void sacrificeAbilityUsesPower() {
        Permanent stalker = addStalker();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrificeForestToDescend();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stalker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    private Permanent addStalker() {
        return addCreatureReady(player1, new StalactiteStalker());
    }

    private void sacrificeForestToDescend() {
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Zuran Orb")),
                null, null);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    void multipleDescentsStillGiveOnlyOneCounter() {
        Permanent stalker = addStalker();
        sacrificeForestToDescend();
        sacrificeForestToDescend();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void descendingAfterEndStepBeginsDoesNotTrigger() {
        Permanent stalker = addStalker();
        advanceToEndStep(player1);
        sacrificeForestToDescend();

        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void descendingBeforeStalkerEntersStillCountsAfterCardLeavesGraveyard() {
        sacrificeForestToDescend();
        harness.setGraveyard(player1, List.of());
        Permanent stalker = addStalker();

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent stalker = addStalker();
        sacrificeForestToDescend();

        advanceToEndStep(player2);

        assertThat(stalker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickStalkerCanWeakenOpponentUntilCleanup() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new StalactiteStalker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StalactiteStalker());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stalker);
        harness.assertInGraveyard(player1, "Stalactite Stalker");
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void negativePowerMakesSacrificeAbilityIncreaseTargetsStats() {
        Permanent stalker = addStalker();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CogworkWrestler()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0, stalker.getId());
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(-1);
        Permanent target = findPermanent(player2, "Cogwork Wrestler");
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }
}
