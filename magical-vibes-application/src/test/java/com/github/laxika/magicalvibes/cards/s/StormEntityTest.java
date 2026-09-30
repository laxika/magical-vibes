package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormEntity.class, Ghostfire.class})
class StormEntityTest extends BaseCardTest {

    @Test
    void entersWithoutCountersAsTheFirstSpellOfTheTurn() {
        harness.setHand(player1, List.of(new StormEntity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Storm Entity")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithOneCounterForEachEarlierSpell() {
        harness.setHand(player1, List.of(new Ghostfire(), new Ghostfire(), new StormEntity()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Storm Entity")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void countsSpellsCastByAnOpponentBeforeItEnters() {
        harness.setHand(player1, List.of(new StormEntity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Ghostfire()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Storm Entity")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void entersWithACounterForAnotherSpellWhenPutOntoTheBattlefieldWithoutBeingCast() {
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.enterBattlefieldAndReturn(player1, new StormEntity());

        assertThat(findPermanent(player1, "Storm Entity")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canAttackImmediatelyBecauseItHasHaste() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new StormEntity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        harness.assertLife(player2, 19);
    }
}
