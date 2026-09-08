package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WinnowingForces;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Forest.class, GrizzlyBears.class, InvasionOfLorwyn.class, LlanowarElves.class,
        WinnowingForces.class})
class InvasionOfLorwynTest extends BaseCardTest {

    @Test
    void entersAndDestroysAnEligibleCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castInvasion(target);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void cannotTargetAnElf() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> castInvasion(target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureAboveLandCount() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> castInvasion(target))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void defeatingTheSiegeCastsWinnowingForcesWithLandScaledPowerAndToughness() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfLorwyn());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent transformed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof WinnowingForces)
                .findFirst()
                .orElseThrow();
        assertThat(transformed.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, transformed)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, transformed)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, transformed)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, transformed)).isEqualTo(3);
    }

    private void castInvasion(Permanent target) {
        harness.setHand(player1, List.of(new InvasionOfLorwyn()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gs.playCard(gd, player1, 0, 0, target.getId(), null);
    }
}
