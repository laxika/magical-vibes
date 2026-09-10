package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MisterHydeMonsterWithin.class, GrizzlyBears.class, Forest.class})
class MisterHydeMonsterWithinTest extends BaseCardTest {

    @Test
    void putsPlusOnePlusOneCounterOnMisterHyde() {
        MisterHydeMonsterWithin hydeCard = new MisterHydeMonsterWithin();
        var hyde = harness.addToBattlefieldAndReturn(player1, hydeCard);

        resolveUpkeepTrigger("Put a +1/+1 counter on Mister Hyde.");

        assertThat(hyde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void removesCounterFromChosenCreatureAndDraws() {
        var hyde = harness.addToBattlefieldAndReturn(player1, new MisterHydeMonsterWithin());
        var bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bear.setCounterCount(CounterType.CHARGE, 1);
        harness.setHand(player1, List.of());
        Forest libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        resolveUpkeepTrigger("Remove a counter from a creature you control. If you do, draw a card.");

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bear.getId());
        harness.handlePermanentChosen(player1, bear.getId());

        assertThat(bear.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerHands.get(player1.getId()).stream().map(card -> card.getId()).toList())
                .containsExactly(libraryCard.getId());
        assertThat(hyde.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void resolveUpkeepTrigger(String mode) {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
    }
}
