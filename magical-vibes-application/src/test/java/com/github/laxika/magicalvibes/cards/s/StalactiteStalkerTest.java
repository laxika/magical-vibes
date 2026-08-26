package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StalactiteStalker.class, Forest.class, GrizzlyBears.class, ZuranOrb.class})
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
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new StalactiteStalker());
        stalker.setSummoningSick(false);
        return stalker;
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
