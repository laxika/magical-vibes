package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenArcher;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.t.TirelessTribe;
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

@CardUsed({Soulcatcher.class, AvenArcher.class, TirelessTribe.class, Firebolt.class})
class SoulcatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when a creature with flying dies")
    void putsCounterWhenFlyingCreatureDies() {
        Permanent soulcatcher = harness.addToBattlefieldAndReturn(player1, new Soulcatcher());
        Permanent avenArcher = harness.addToBattlefieldAndReturn(player2, new AvenArcher());

        killWithFirebolt(player2, avenArcher);

        assertThat(soulcatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers for a flying creature controlled by its controller")
    void putsCounterWhenOwnFlyingCreatureDies() {
        Permanent soulcatcher = harness.addToBattlefieldAndReturn(player1, new Soulcatcher());
        Permanent avenArcher = harness.addToBattlefieldAndReturn(player1, new AvenArcher());

        killWithFirebolt(player1, avenArcher);

        assertThat(soulcatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a creature without flying dies")
    void doesNotTriggerWhenNonFlyingCreatureDies() {
        Permanent soulcatcher = harness.addToBattlefieldAndReturn(player1, new Soulcatcher());
        Permanent tirelessTribe = harness.addToBattlefieldAndReturn(player2, new TirelessTribe());

        killWithFirebolt(player2, tirelessTribe);

        assertThat(soulcatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    private void killWithFirebolt(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Firebolt()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveSorcery(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
