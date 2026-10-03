package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HazeOfPollen;
import com.github.laxika.magicalvibes.cards.l.LotusBloom;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.PullFromTomorrow;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AsForetold.class, GrizzlyBears.class, Opt.class, HazeOfPollen.class,
        LotusBloom.class, PullFromTomorrow.class, SongOfTheDryads.class})
class AsForetoldTest extends BaseCardTest {

    @Test
    @DisplayName("A spell with mana value at most the time-counter count can be cast for {0}")
    void castsQualifyingSpellForFree() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        // Grizzly Bears costs {1}{G} (mana value 2). With 2 time counters it may be cast for {0}.
        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        // No mana added — it should still be castable.

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(spell);
    }

    @Test
    @DisplayName("Casting for {0} spends no mana")
    void freeCastSpendsNoMana() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(5);
    }

    @Test
    @DisplayName("A spell with mana value above the time-counter count cannot be cast for {0}")
    void spellAboveCounterCapIsNotFree() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 1);
        // Grizzly Bears (mana value 2) exceeds the single time counter, so it is not free.
        harness.setHand(player1, List.of(new GrizzlyBears()));
        // No mana — casting must fail.

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only one spell per turn may be cast for {0}")
    void freeCastLimitedToOncePerTurn() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 1);
        // Opt costs {U} (mana value 1); both qualify for the {0} cost.
        harness.setHand(player1, List.of(new Opt(), new Opt()));
        // No mana — only the first cast can be free.

        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("At the beginning of the controller's upkeep, a time counter is added")
    void upkeepTriggerAddsTimeCounter() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve PutCountersOnSelfEffect

        assertThat(asForetold.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    void opponentsUpkeepDoesNotAddTimeCounter() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(asForetold.getCounterCount(CounterType.TIME)).isZero();
    }

    @Test
    void opponentCannotUseTheAlternativeCost() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player2, List.of(new HazeOfPollen()));
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleCopiesEachAllowOneFreeSpell() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        first.setCounterCount(CounterType.TIME, 2);
        second.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player1, List.of(new HazeOfPollen(), new HazeOfPollen(), new HazeOfPollen()));

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void freeCastResetsOnOpponentsTurn() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player1, List.of(new HazeOfPollen(), new HazeOfPollen()));
        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(asForetold.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    void xMustBeZeroWhenUsingTheFreeCost() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 5);
        harness.setHand(player1, List.of(new PullFromTomorrow()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void xSpellCanBeCastWithXZeroWithoutMana() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player1, List.of(new PullFromTomorrow()));

        harness.castInstant(player1, 0, 0, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getXValue()).isZero();
    }

    @Test
    void spellWithoutManaCostUsesTheOncePerTurnAllowance() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player1, List.of(new LotusBloom(), new HazeOfPollen()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void spellWithoutManaCostCanBeCastWithNoTimeCounters() {
        harness.addToBattlefield(player1, new AsForetold());
        harness.setHand(player1, List.of(new LotusBloom()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lotus Bloom");
    }

    @Test
    void payingNormallyForANonzeroXPreservesTheFreeCast() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player1, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 5);
        harness.setHand(player1, List.of(new PullFromTomorrow(), new HazeOfPollen()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, 2, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void losingPrintedAbilitiesRemovesTheAlternativeCost() {
        Permanent asForetold = harness.addToBattlefieldAndReturn(player2, new AsForetold());
        asForetold.setCounterCount(CounterType.TIME, 2);
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, asForetold.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new HazeOfPollen()));

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
