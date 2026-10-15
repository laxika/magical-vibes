package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.cards.r.RiphookRaider;
import com.github.laxika.magicalvibes.cards.s.SporeCrawler;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HookhandMariner.class, RiphookRaider.class, DawnhartDisciple.class, SporeCrawler.class})
class HookhandMarinerTest extends BaseCardTest {

    @Test
    void entersAsHookhandMarinerDuringDay() {
        gd.dayNight = DayNight.DAY;

        Permanent mariner = harness.enterBattlefieldAndReturn(player1, new HookhandMariner());

        assertThat(mariner.isTransformed()).isFalse();
        assertThat(mariner.getCard()).isInstanceOf(HookhandMariner.class);
    }

    @Test
    void entersAsRiphookRaiderDuringNight() {
        gd.dayNight = DayNight.NIGHT;

        Permanent mariner = harness.enterBattlefieldAndReturn(player1, new HookhandMariner());

        assertThat(mariner.isTransformed()).isTrue();
        assertThat(mariner.getCard()).isInstanceOf(RiphookRaider.class);
    }

    @Test
    void transformsWithDayAndNight() {
        gd.dayNight = DayNight.DAY;
        Permanent mariner = harness.enterBattlefieldAndReturn(player1, new HookhandMariner());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(mariner.getCard()).isInstanceOf(RiphookRaider.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(mariner.getCard()).isInstanceOf(HookhandMariner.class);
    }

    @Test
    void riphookRaiderCannotBeBlockedByPowerTwoOrLess() {
        Permanent blocker = addCreatureReady(player2, new DawnhartDisciple());
        Permanent raider = addTransformedMariner(player1);
        raider.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(raider);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void riphookRaiderCanBeBlockedByPowerGreaterThanTwo() {
        Permanent blocker = addCreatureReady(player2, new SporeCrawler());
        Permanent raider = addTransformedMariner(player1);
        raider.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(raider);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void enteringWhenNeitherDayNorNightStartsDay() {
        gd.dayNight = DayNight.NEITHER;

        Permanent mariner = harness.enterBattlefieldAndReturn(player1, new HookhandMariner());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(mariner.isTransformed()).isFalse();
    }

    @Test
    void castingDuringNightResolvesAsBackFace() {
        gd.dayNight = DayNight.NIGHT;

        harness.castFromHand(player1, new HookhandMariner(), "{3}{G}");
        harness.passBothPriorities();

        Permanent raider = findPermanent(player1, "Riphook Raider");
        assertThat(raider.isTransformed()).isTrue();
        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
    }

    @Test
    void dayUsesOnlyPreviousActivePlayersSpellCount() {
        gd.dayNight = DayNight.DAY;
        Permanent mariner = harness.enterBattlefieldAndReturn(player1, new HookhandMariner());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(mariner.getCard()).isInstanceOf(RiphookRaider.class);
    }

    @Test
    void oneSpellByPreviousActivePlayerKeepsDay() {
        gd.dayNight = DayNight.DAY;
        Permanent mariner = harness.enterBattlefieldAndReturn(player1, new HookhandMariner());
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(mariner.getCard()).isInstanceOf(HookhandMariner.class);
    }

    @Test
    void opponentsSpellsDoNotMakeNightBecomeDay() {
        Permanent raider = addTransformedMariner(player1);
        gd.previousTurnActivePlayerId = player1.getId();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player2);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(raider.getCard()).isInstanceOf(RiphookRaider.class);
    }

    @Test
    void frontFaceCanBeBlockedByPowerTwo() {
        Permanent blocker = addCreatureReady(player2, new DawnhartDisciple());
        gd.dayNight = DayNight.DAY;
        Permanent mariner = harness.enterBattlefieldAndReturn(player1, new HookhandMariner());
        mariner.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void increasedPowerAllowsAnOtherwiseProhibitedBlocker() {
        Permanent blocker = addCreatureReady(player2, new DawnhartDisciple());
        blocker.setPowerModifier(1);
        Permanent raider = addTransformedMariner(player1);
        raider.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void decreasedPowerProhibitsAnOtherwiseLegalBlocker() {
        Permanent blocker = addCreatureReady(player2, new SporeCrawler());
        blocker.setPowerModifier(-1);
        Permanent raider = addTransformedMariner(player1);
        raider.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTransformedMariner(Player player) {
        gd.dayNight = DayNight.NIGHT;
        Permanent mariner = harness.enterBattlefieldAndReturn(player, new HookhandMariner());
        mariner.setSummoningSick(false);
        return mariner;
    }
}
