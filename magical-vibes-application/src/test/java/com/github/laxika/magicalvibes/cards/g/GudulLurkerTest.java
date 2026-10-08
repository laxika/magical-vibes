package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BreakOpen;
import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GudulLurker.class, ColossodonYearling.class, BreakOpen.class})
class GudulLurkerTest extends BaseCardTest {

    @Test
    void cannotBeBlocked() {
        addCreatureReady(player2, new ColossodonYearling());
        Permanent lurker = addCreatureReady(player1, new GudulLurker());
        lurker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void megamorphPutsPlusOneCounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new GudulLurker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent lurker = findPermanent(player1, "Gudul Lurker");
        assertThat(lurker.isFaceDown()).isTrue();
        assertThat(lurker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lurker));

        assertThat(lurker.isFaceDown()).isFalse();
        assertThat(lurker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canBeBlockedWhileFaceDown() {
        Permanent blocker = addCreatureReady(player2, new ColossodonYearling());
        Permanent lurker = addCreatureReady(player1, new GudulLurker());
        lurker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        lurker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void turningFaceUpWithoutPayingMegamorphDoesNotAddCounter() {
        Permanent lurker = harness.addToBattlefieldAndReturn(player2, new GudulLurker());
        lurker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player1, List.of(new BreakOpen()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, lurker.getId());

        assertThat(lurker.isFaceDown()).isFalse();
        assertThat(lurker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
