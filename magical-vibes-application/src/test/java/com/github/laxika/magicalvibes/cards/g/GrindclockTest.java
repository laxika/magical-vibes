package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({Grindclock.class, Shatter.class})
class GrindclockTest extends BaseCardTest {


    @Test
    @DisplayName("Tapping Grindclock puts a charge counter on it")
    void tappingAddsChargeCounter() {
        Permanent grindclock = addReadyGrindclock(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(grindclock.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability 0 taps Grindclock")
    void ability0TapsGrindclock() {
        Permanent grindclock = addReadyGrindclock(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(grindclock.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple activations accumulate charge counters")
    void multipleActivationsAccumulateCounters() {
        Permanent grindclock = addReadyGrindclock(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(grindclock.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        grindclock.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(grindclock.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        grindclock.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(grindclock.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate ability 0 when already tapped")
    void cannotActivateAbility0WhenTapped() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }


    @Test
    @DisplayName("Activating ability 1 targeting player puts it on the stack")
    void ability1PutsOnStack() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getSourcePermanentId()).isEqualTo(grindclock.getId());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Mill effect mills X cards where X is charge counters")
    void millsByChargeCounters() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 3);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mill effect mills 1 card with 1 charge counter")
    void millsOneCardWithOneCounter() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Mill effect mills 0 cards with 0 charge counters")
    void millsNothingWithZeroCounters() {
        Permanent grindclock = addReadyGrindclock(player1);
        // No charge counters set

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Milled cards come from the top of the library")
    void milledCardsFromTopOfLibrary() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 2);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 5) {
            deck.removeFirst();
        }
        Card firstCard = deck.get(0);
        Card secondCard = deck.get(1);
        Card thirdCard = deck.get(2);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isEqualTo(thirdCard);
    }

    @Test
    @DisplayName("Can target yourself with mill ability")
    void canTargetSelfWithMill() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mill is capped by library size when counters exceed deck")
    void millCappedByLibrarySize() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 10);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 3) {
            deck.removeFirst();
        }

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mill does nothing when library is empty")
    void millNothingWhenLibraryEmpty() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 5);

        gd.playerDecks.get(player2.getId()).clear();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Ability 1 taps Grindclock")
    void ability1TapsGrindclock() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(grindclock.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability 1 when already tapped")
    void cannotActivateAbility1WhenTapped() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 3);
        grindclock.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }


    @Test
    @DisplayName("Cannot use both abilities in same turn (both require tap)")
    void cannotUseBothAbilitiesInSameTurn() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 1);

        // Use ability 0 first
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Grindclock is now tapped, cannot use ability 1
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }


    @Test
    @DisplayName("Charge counters are preserved after using mill ability")
    void chargeCountersPreservedAfterMill() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        // Grindclock still has 3 charge counters (mill doesn't remove them)
        assertThat(grindclock.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }


    @Test
    @DisplayName("Can activate ability the turn it enters the battlefield")
    void noSummoningSicknessForArtifact() {
        Permanent grindclock = harness.addToBattlefieldAndReturn(player1, new Grindclock());
        grindclock.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(grindclock.isTapped()).isTrue();
    }


    @Test
    @DisplayName("Charge counter is added on resolution, not as an activation cost")
    void counterAddedOnlyOnResolution() {
        Permanent grindclock = addReadyGrindclock(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(grindclock.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();
        assertThat(grindclock.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mill counts charge counters at resolution and ignores other counters")
    void millUsesCountersAtResolution() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 1);
        grindclock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        grindclock.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(grindclock.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mill uses the counters present when Grindclock leaves the battlefield")
    void millUsesLastKnownCountersAfterDestruction() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setCounterCount(CounterType.CHARGE, 1);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        grindclock.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(grindclock.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, grindclock.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Counter ability cannot put a counter on a destroyed Grindclock")
    void counterAbilityDoesNothingAfterDestruction() {
        Permanent grindclock = addReadyGrindclock(player1);
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, grindclock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(grindclock.getCard()).hasSize(2);
        assertThat(grindclock.getCounterCount(CounterType.CHARGE)).isZero();
    }

    private Permanent addReadyGrindclock(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Grindclock());
        perm.setSummoningSick(false);
        return perm;
    }
}
