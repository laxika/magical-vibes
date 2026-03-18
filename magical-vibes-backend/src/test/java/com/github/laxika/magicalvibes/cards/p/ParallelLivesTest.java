package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParallelLivesTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Parallel Lives has a static DoubleTokenCreationEffect")
    void hasCorrectEffects() {
        ParallelLives card = new ParallelLives();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(MultiplyTokenCreationEffect.class);
        MultiplyTokenCreationEffect effect = (MultiplyTokenCreationEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.multiplier()).isEqualTo(2);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Parallel Lives puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ParallelLives()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Parallel Lives");
    }

    @Test
    @DisplayName("Resolving Parallel Lives puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new ParallelLives()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Parallel Lives"));
    }

    // ===== Doubles ETB token creation =====

    @Test
    @DisplayName("Blade Splicer ETB creates 2 Golem tokens instead of 1 with Parallel Lives")
    void doublesEtbTokenCreation() {
        harness.addToBattlefield(player1, new ParallelLives());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(tokens).hasSize(2);
    }

    // ===== Does not affect opponent's tokens =====

    @Test
    @DisplayName("Parallel Lives does not double tokens for the opponent")
    void doesNotDoubleOpponentTokens() {
        harness.addToBattlefield(player1, new ParallelLives());
        harness.setHand(player2, List.of(new BladeSplicer()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> opponentTokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(opponentTokens).hasSize(1);
    }

    // ===== Multiple Parallel Lives stack multiplicatively =====

    @Test
    @DisplayName("Two Parallel Lives quadruple tokens (1 -> 4)")
    void twoParallelLivesQuadrupleTokens() {
        harness.addToBattlefield(player1, new ParallelLives());
        harness.addToBattlefield(player1, new ParallelLives());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(tokens).hasSize(4);
    }

    // ===== Without Parallel Lives, normal token count =====

    @Test
    @DisplayName("Without Parallel Lives, Blade Splicer creates exactly 1 token")
    void noDoublingWithoutParallelLives() {
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(tokens).hasSize(1);
    }

    // ===== Effect removed when Parallel Lives leaves =====

    @Test
    @DisplayName("Removing Parallel Lives from battlefield stops doubling")
    void removingParallelLivesStopsDoubling() {
        harness.addToBattlefield(player1, new ParallelLives());

        // First cast: doubled tokens
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokensAfterFirst = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(tokensAfterFirst).hasSize(2);

        // Remove Parallel Lives from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Parallel Lives"));

        // Second cast: normal tokens
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> tokensAfterSecond = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        // 2 from first cast + 1 from second cast = 3
        assertThat(tokensAfterSecond).hasSize(3);
    }

    // ===== Opponent's Parallel Lives doubles their own tokens =====

    @Test
    @DisplayName("Opponent's Parallel Lives doubles their own tokens but not yours")
    void opponentParallelLivesDoublesTheirTokens() {
        harness.addToBattlefield(player2, new ParallelLives());
        harness.setHand(player2, List.of(new BladeSplicer()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Opponent's tokens are doubled
        List<Permanent> opponentTokens = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(opponentTokens).hasSize(2);

        // Player 1's tokens are NOT doubled (no Parallel Lives on their side)
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> myTokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .toList();
        assertThat(myTokens).hasSize(1);
    }
}
