package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.DutifulKnowledgeSeeker;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeaconOfImmortality.class, Cancel.class, DutifulKnowledgeSeeker.class, PlatinumAngel.class})
class BeaconOfImmortalityTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Beacon of Immortality puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0, player1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Resolving doubles target player's life total from 20 to 40")
    void doublesLifeFrom20To40() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 40);
        assertThat(gameLogContains("doubled from 20 to 40")).isTrue();
    }

    @Test
    @DisplayName("Can target opponent to double their life")
    void canTargetOpponent() {
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Doubles low life total correctly")
    void doublesLowLifeTotal() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 6);
    }

    @Test
    @DisplayName("Beacon is shuffled into library instead of going to graveyard")
    void shuffledIntoLibraryNotGraveyard() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, player1.getId());

        GameData gd = harness.getGameData();
        // Not in graveyard
        harness.assertNotInGraveyard(player1, "Beacon of Immortality");
        // In library (deck size increased by 1)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        // Card exists somewhere in the deck
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Beacon of Immortality"));
        // Log confirms shuffle
        assertThat(gameLogContains("shuffled into its owner's library")).isTrue();
    }

    @Test
    @DisplayName("Shuffling Beacon into a library triggers library-put abilities")
    void shufflingIntoLibraryTriggersLibraryPutAbilities() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player2, new DutifulKnowledgeSeeker());
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Uses the target's life total when the spell resolves")
    void usesLifeTotalAtResolution() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.setLife(player1, 7);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        assertThat(gd.lifeGainedThisTurn.get(player1.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("Doubling a negative life total loses life")
    void doublesNegativeLifeTotal() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, -3);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, -6);
        harness.assertNotInGraveyard(player1, "Beacon of Immortality");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card instanceof BeaconOfImmortality);
    }

    @Test
    @DisplayName("At zero life Beacon still shuffles into its owner's library")
    void zeroLifeStillShuffles() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, 0);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 0);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card instanceof BeaconOfImmortality);
        harness.assertNotInGraveyard(player1, "Beacon of Immortality");
    }

    @Test
    @DisplayName("A countered Beacon goes to the graveyard without doubling life or shuffling")
    void counteredBeaconDoesNotShuffle() {
        BeaconOfImmortality beacon = new BeaconOfImmortality();
        harness.setHand(player1, List.of(beacon));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player2, ManaColor.BLUE, 3);
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, beacon.getId());

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Beacon of Immortality");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize)
                .doesNotContain(beacon);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Beacon cannot target a permanent")
    void cannotTargetPermanent() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new PlatinumAngel());
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, angel.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Beacon of Immortality");
        harness.assertLife(player1, 20);
    }
}
