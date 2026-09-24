package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.CranialPlating;
import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeaconOfDestruction.class, DrossCrocodile.class, ChandraNalaar.class, CranialPlating.class})
class BeaconOfDestructionTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Beacon of Destruction targeting a player puts it on the stack")
    void castingTargetingPlayerPutsItOnStack() {
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Beacon of Destruction targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsItOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = creature.getId();
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CranialPlating());
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Dealing damage to player =====

    @Test
    @DisplayName("Deals 5 damage to target player")
    void deals5DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Can target yourself to deal 5 damage")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    // ===== Dealing damage to creature =====

    @Test
    @DisplayName("Deals 5 damage to target creature, destroying it")
    void deals5DamageToCreatureDestroysIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Dross Crocodile");
        harness.assertInGraveyard(player2, "Dross Crocodile");
    }

    @Test
    @DisplayName("Deals 5 damage to target planeswalker")
    void deals5DamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).contains(planeswalker);
    }

    // ===== Shuffle into library =====

    @Test
    @DisplayName("Beacon is shuffled into library instead of going to graveyard")
    void shuffledIntoLibraryNotGraveyard() {
        harness.setLife(player2, 20);
        BeaconOfDestruction beacon = new BeaconOfDestruction();
        harness.setHand(player1, List.of(beacon));
        harness.addMana(player1, ManaColor.RED, 5);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(beacon);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(beacon);
        assertThat(gameLogContains("shuffled into its owner's library")).isTrue();
    }

    @Test
    @DisplayName("Beacon is shuffled into library even when targeting a creature")
    void shuffledIntoLibraryWhenTargetingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrossCrocodile());
        BeaconOfDestruction beacon = new BeaconOfDestruction();
        harness.setHand(player1, List.of(beacon));
        harness.addMana(player1, ManaColor.RED, 5);

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(beacon);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).contains(beacon);
    }

    // ===== Stack cleanup =====

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BeaconOfDestruction()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().stack).isEmpty();
    }
}

