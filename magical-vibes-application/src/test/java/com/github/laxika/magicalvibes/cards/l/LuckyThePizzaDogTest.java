package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CaptainAmericaTeamLeader;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeroOfThePride;
import com.github.laxika.magicalvibes.cards.p.PackLeader;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({LuckyThePizzaDog.class, HeroOfThePride.class, PackLeader.class,
        CaptainAmericaTeamLeader.class, GrizzlyBears.class})
class LuckyThePizzaDogTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cat, Dog, and Hero spells creates Food tokens")
    void matchingSubtypeSpellsCreateFood() {
        harness.addToBattlefield(player1, new LuckyThePizzaDog());
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        castAndResolve(new HeroOfThePride());
        castAndResolve(new PackLeader());
        castAndResolve(new CaptainAmericaTeamLeader());

        assertThat(countPermanents(player1, "Food")).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature spell without a matching subtype does not create Food")
    void nonmatchingSubtypeDoesNotCreateFood() {
        harness.addToBattlefield(player1, new LuckyThePizzaDog());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveStack();

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Gaining life puts a +1/+1 counter on Lucky at each end step")
    void gainedLifePutsCounterAtAnyEndStep() {
        Permanent lucky = harness.addToBattlefieldAndReturn(player1, new LuckyThePizzaDog());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(lucky.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lucky does not get a counter when you did not gain life")
    void noCounterWithoutLifeGain() {
        Permanent lucky = harness.addToBattlefieldAndReturn(player1, new LuckyThePizzaDog());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(lucky.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castAndResolve(Card card) {
        harness.setHand(player1, List.of(card));
        harness.castCreature(player1, 0);
        resolveStack();
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
