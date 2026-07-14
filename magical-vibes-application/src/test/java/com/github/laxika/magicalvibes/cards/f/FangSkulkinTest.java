package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FangSkulkinTest extends BaseCardTest {

    private Permanent addSkulkin() {
        Permanent skulkin = harness.addToBattlefieldAndReturn(player1, new FangSkulkin());
        skulkin.setSummoningSick(false);
        return skulkin;
    }

    @Test
    @DisplayName("Grants wither to a black creature until end of turn, then it wears off")
    void grantsWitherToBlackCreature() {
        addSkulkin();
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player1, new BlackKnight());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Black Knight");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blackCreature, Keyword.WITHER)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blackCreature, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("The granted wither makes combat damage a -1/-1 counter")
    void witherDealsMinusCounters() {
        addSkulkin();
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BlackKnight()); // 2/2 black
        attacker.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Black Knight");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GiantSpider()); // 2/4
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Cannot target a non-black creature")
    void cannotTargetNonBlackCreature() {
        addSkulkin();
        harness.addToBattlefield(player1, new GrizzlyBears()); // green
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
