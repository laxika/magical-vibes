package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.m.MightOfTheMeek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BraveMeadowguard.class, GiantGrowth.class, MightOfTheMeek.class})
class BraveMeadowguardTest extends BaseCardTest {

    @Test
    void entersAndConjuresMightOfTheMeekIntoHand() {
        harness.enterBattlefieldAndReturn(player1, new BraveMeadowguard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anySatisfy(card -> {
                    assertThat(card.getName()).isEqualTo("Might of the Meek");
                    assertThat(card.isToken()).isFalse();
                });
    }

    @Test
    void valiantPutsOnlyOneCounterOnTheFirstSpellYouControlEachTurn() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new BraveMeadowguard());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, guard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castInstant(player1, 0, guard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void valiantDoesNotTriggerForAnOpponentsSpell() {
        Permanent guard = harness.addToBattlefieldAndReturn(player1, new BraveMeadowguard());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, guard.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(guard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
