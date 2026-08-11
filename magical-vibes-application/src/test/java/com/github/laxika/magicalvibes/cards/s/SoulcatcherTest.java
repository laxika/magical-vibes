package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulcatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when a creature with flying dies")
    void putsCounterWhenFlyingCreatureDies() {
        Permanent soulcatcher = harness.addToBattlefieldAndReturn(player1, new Soulcatcher());
        Permanent cloudSprite = harness.addToBattlefieldAndReturn(player2, new CloudSprite());

        killWithShock(player2, cloudSprite);

        assertThat(soulcatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a creature without flying dies")
    void doesNotTriggerWhenNonFlyingCreatureDies() {
        Permanent soulcatcher = harness.addToBattlefieldAndReturn(player1, new Soulcatcher());
        Permanent savannahLions = harness.addToBattlefieldAndReturn(player2, new SavannahLions());

        killWithShock(player2, savannahLions);

        assertThat(soulcatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    private void killWithShock(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
