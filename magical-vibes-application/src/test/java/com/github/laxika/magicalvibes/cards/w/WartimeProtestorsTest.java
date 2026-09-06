package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JoinTheRanks;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WartimeProtestors.class, JoinTheRanks.class, GrizzlyBears.class})
class WartimeProtestorsTest extends BaseCardTest {

    @Test
    @DisplayName("Each Ally entering under your control gets a +1/+1 counter and haste")
    void alliesEnteringGetCounterAndHaste() {
        harness.addToBattlefield(player1, new WartimeProtestors());

        harness.setHand(player1, List.of(new JoinTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> allies = findPermanents(player1, "Soldier Ally");
        assertThat(allies).hasSize(2);
        assertThat(allies).allSatisfy(ally -> {
            assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(ally.hasKeyword(Keyword.HASTE)).isTrue();
        });
    }

    @Test
    @DisplayName("The counter remains while granted haste expires at end of turn")
    void counterRemainsAfterHasteExpires() {
        harness.addToBattlefield(player1, new WartimeProtestors());

        harness.setHand(player1, List.of(new JoinTheRanks()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> allies = findPermanents(player1, "Soldier Ally");
        assertThat(allies).allSatisfy(ally -> {
            assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(ally.hasKeyword(Keyword.HASTE)).isFalse();
        });
    }

    @Test
    @DisplayName("A non-Ally creature does not trigger Wartime Protestors")
    void nonAllyDoesNotTrigger() {
        harness.addToBattlefield(player1, new WartimeProtestors());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Wartime Protestors does not trigger for its own entry")
    void ownEntryDoesNotTrigger() {
        harness.setHand(player1, List.of(new WartimeProtestors()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Wartime Protestors")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
