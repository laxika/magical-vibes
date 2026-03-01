package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutXMinusOneMinusOneCountersOnEachCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlackSunsZenithTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Black Sun's Zenith has correct effects")
    void hasCorrectEffects() {
        BlackSunsZenith card = new BlackSunsZenith();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(PutXMinusOneMinusOneCountersOnEachCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ShuffleIntoLibraryEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack with correct X value")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=3: {3}{B}{B} = 5

        harness.castSorcery(player1, 0, 3);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Black Sun's Zenith");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    // ===== Resolution: -1/-1 counters =====

    @Test
    @DisplayName("X=2 puts two -1/-1 counters on each creature")
    void putsXCountersOnEachCreature() {
        Permanent bear1 = addCreature(player1, new GrizzlyBears());
        Permanent bear2 = addCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2: {2}{B}{B} = 4

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        // Both 2/2 bears get 2 -1/-1 counters → 0/0 → die to SBA
        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("X=1 puts one -1/-1 counter, small creatures survive with reduced stats")
    void x1ReducesButDoesNotKillBears() {
        Permanent bear = addCreature(player1, new GrizzlyBears()); // 2/2

        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=1: {1}{B}{B} = 3

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        // Bear is now 1/1 (2/2 with one -1/-1 counter)
        assertThat(bear.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Large X kills small creatures but not large ones")
    void largeXKillsSmallButNotLargeCreatures() {
        addCreature(player1, new GrizzlyBears()); // 2/2
        Permanent bigCreature = addCreature(player2, bigCreature()); // 4/5

        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=3: {3}{B}{B} = 5

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 2/2 bear gets 3 -1/-1 counters → -1/-1 → dies
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // 4/5 gets 3 -1/-1 counters → 1/2 → survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Big Creature"));
        assertThat(bigCreature.getMinusOneMinusOneCounters()).isEqualTo(3);
    }

    @Test
    @DisplayName("X=0 resolves with no counters placed")
    void xZeroPlacesNoCounters() {
        Permanent bear = addCreature(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 2); // X=0: {0}{B}{B} = 2

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(bear.getMinusOneMinusOneCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Counters are permanent and persist across turns")
    void countersPersistAcrossTurns() {
        Permanent bigCreature = addCreature(player1, bigCreature()); // 4/5

        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 4); // X=2

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(bigCreature.getMinusOneMinusOneCounters()).isEqualTo(2);

        // Counters still there after moving to next turn
        harness.forceStep(null);
        assertThat(bigCreature.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    // ===== Shuffle into library =====

    @Test
    @DisplayName("Black Sun's Zenith is shuffled into library instead of going to graveyard")
    void shuffledIntoLibraryNotGraveyard() {
        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 3); // X=1

        int deckSizeBefore = harness.getGameData().playerDecks.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Not in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Black Sun's Zenith"));
        // In library (deck size increased by 1)
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        // Card exists somewhere in the deck
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Black Sun's Zenith"));
    }

    // ===== Stack cleanup =====

    @Test
    @DisplayName("Stack is empty after resolution")
    void stackIsEmptyAfterResolution() {
        harness.setHand(player1, List.of(new BlackSunsZenith()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private static Card bigCreature() {
        Card card = new Card();
        card.setName("Big Creature");
        card.setType(CardType.CREATURE);
        card.setManaCost("{3}{G}{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(4);
        card.setToughness(5);
        return card;
    }
}
