package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrindclockTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Grindclock has two activated abilities with correct structure")
    void hasCorrectAbilityStructure() {
        Grindclock card = new Grindclock();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // Ability 0: {T}: Put a charge counter on Grindclock.
        var ability0 = card.getActivatedAbilities().get(0);
        assertThat(ability0.isRequiresTap()).isTrue();
        assertThat(ability0.getManaCost()).isNull();
        assertThat(ability0.getEffects()).hasSize(1);
        assertThat(ability0.getEffects().getFirst()).isInstanceOf(PutChargeCounterOnSelfEffect.class);

        // Ability 1: {T}: Target player mills X cards, where X is the number of charge counters on Grindclock.
        var ability1 = card.getActivatedAbilities().get(1);
        assertThat(ability1.isRequiresTap()).isTrue();
        assertThat(ability1.getManaCost()).isNull();
        assertThat(ability1.getEffects()).hasSize(1);
        assertThat(ability1.getEffects().getFirst()).isInstanceOf(MillTargetPlayerByChargeCountersEffect.class);
        assertThat(ability1.isNeedsTarget()).isTrue();
    }

    // ===== Ability 0: Put a charge counter =====

    @Test
    @DisplayName("Tapping Grindclock puts a charge counter on it")
    void tappingAddsChargeCounter() {
        Permanent grindclock = addReadyGrindclock(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(grindclock.getChargeCounters()).isEqualTo(1);
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
        assertThat(grindclock.getChargeCounters()).isEqualTo(1);

        grindclock.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(grindclock.getChargeCounters()).isEqualTo(2);

        grindclock.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(grindclock.getChargeCounters()).isEqualTo(3);
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

    // ===== Ability 1: Mill by charge counters =====

    @Test
    @DisplayName("Activating ability 1 targeting player puts it on the stack")
    void ability1PutsOnStack() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setChargeCounters(3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Grindclock");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Mill effect mills X cards where X is charge counters")
    void millsByChargeCounters() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setChargeCounters(3);

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
        grindclock.setChargeCounters(1);

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
        grindclock.setChargeCounters(2);

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
        grindclock.setChargeCounters(2);

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
        grindclock.setChargeCounters(10);

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
        grindclock.setChargeCounters(5);

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
        grindclock.setChargeCounters(1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        assertThat(grindclock.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability 1 when already tapped")
    void cannotActivateAbility1WhenTapped() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setChargeCounters(3);
        grindclock.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Both abilities share tap =====

    @Test
    @DisplayName("Cannot use both abilities in same turn (both require tap)")
    void cannotUseBothAbilitiesInSameTurn() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setChargeCounters(1);

        // Use ability 0 first
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Grindclock is now tapped, cannot use ability 1
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Charge counters are preserved after mill =====

    @Test
    @DisplayName("Charge counters are preserved after using mill ability")
    void chargeCountersPreservedAfterMill() {
        Permanent grindclock = addReadyGrindclock(player1);
        grindclock.setChargeCounters(3);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        // Grindclock still has 3 charge counters (mill doesn't remove them)
        assertThat(grindclock.getChargeCounters()).isEqualTo(3);
    }

    // ===== No summoning sickness for artifacts =====

    @Test
    @DisplayName("Can activate ability the turn it enters the battlefield")
    void noSummoningSicknessForArtifact() {
        Grindclock card = new Grindclock();
        Permanent grindclock = new Permanent(card);
        grindclock.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(grindclock);

        harness.activateAbility(player1, 0, null, null);

        assertThat(grindclock.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyGrindclock(Player player) {
        Grindclock card = new Grindclock();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
