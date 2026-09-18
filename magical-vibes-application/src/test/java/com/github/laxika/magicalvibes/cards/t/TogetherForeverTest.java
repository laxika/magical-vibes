package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TogetherForever.class, GrizzlyBears.class, LightningBolt.class})
class TogetherForeverTest extends BaseCardTest {

    @Test
    @DisplayName("Support 2 puts a +1/+1 counter on each of up to two target creatures")
    void supportPutsCountersOnTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TogetherForever()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns the targeted creature to its owner's hand when it dies this turn")
    void returnsTargetedCreatureToOwnersHandWhenItDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent togetherForever = harness.addToBattlefieldAndReturn(player1, new TogetherForever());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(togetherForever);
        harness.activateAbility(player1, sourceIndex, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature without a counter")
    void cannotTargetCreatureWithoutCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent togetherForever = harness.addToBattlefieldAndReturn(player1, new TogetherForever());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(togetherForever);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with a counter on it");
    }
}
