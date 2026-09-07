package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RiphookRaider;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HookhandMariner.class, RiphookRaider.class, GrizzlyBears.class, HillGiant.class})
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
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent raider = addTransformedMariner(player1);
        raider.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(raider);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addTransformedMariner(Player player) {
        HookhandMariner card = new HookhandMariner();
        Permanent mariner = new Permanent(card);
        mariner.setSummoningSick(false);
        mariner.setCard(card.getBackFaceCard());
        mariner.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(mariner);
        return mariner;
    }
}
