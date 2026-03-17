package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BoostSelfBySlimeCountersOnLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutSlimeCounterAndCreateOozeTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GutterGrimeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_ALLY_NONTOKEN_CREATURE_DIES PutSlimeCounterAndCreateOozeTokenEffect")
    void hasCorrectEffects() {
        GutterGrime card = new GutterGrime();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES).getFirst())
                .isInstanceOf(PutSlimeCounterAndCreateOozeTokenEffect.class);
    }

    // ===== Triggered ability =====

    @Nested
    @DisplayName("Triggered ability")
    class TriggeredAbilityTests {

        @Test
        @DisplayName("Gutter Grime survives: creates Ooze token with P/T equal to slime counters")
        void gutterGrimeSurvivesCreatesCorrectToken() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player1, new GrizzlyBears());

            // Kill the bear with an opponent's Wrath so Gutter Grime (enchantment) survives
            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — bear dies

            GameData gd = harness.getGameData();

            // Gutter Grime trigger should be on the stack
            assertThat(gd.stack).isNotEmpty();
            harness.passBothPriorities(); // Resolve the trigger

            // Gutter Grime should have 1 slime counter
            Permanent grime = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Gutter Grime"))
                    .findFirst().orElse(null);
            assertThat(grime).isNotNull();
            assertThat(grime.getSlimeCounters()).isEqualTo(1);

            // One Ooze token should exist
            List<Permanent> oozes = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Ooze"))
                    .toList();
            assertThat(oozes).hasSize(1);

            Permanent ooze = oozes.getFirst();
            assertThat(ooze.getCard().isToken()).isTrue();
            assertThat(ooze.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(ooze.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(ooze.getCard().getSubtypes()).contains(CardSubtype.OOZE);

            // Token base P/T is 0/0 but static effect gives it +1/+1
            var bonus = gqs.computeStaticBonus(gd, ooze);
            assertThat(ooze.getCard().getPower() + bonus.power()).isEqualTo(1);
            assertThat(ooze.getCard().getToughness() + bonus.toughness()).isEqualTo(1);
        }

        @Test
        @DisplayName("Multiple creature deaths produce multiple slime counters and tokens")
        void multipleDeathsMultipleTokens() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());

            // Use opponent's Wrath so Gutter Grime survives
            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — both bears die

            GameData gd = harness.getGameData();

            // Two triggers on the stack
            assertThat(gd.stack).hasSize(2);

            harness.passBothPriorities(); // Resolve first trigger
            harness.passBothPriorities(); // Resolve second trigger

            // Gutter Grime should have 2 slime counters
            Permanent grime = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Gutter Grime"))
                    .findFirst().orElse(null);
            assertThat(grime).isNotNull();
            assertThat(grime.getSlimeCounters()).isEqualTo(2);

            // Two Ooze tokens should exist
            List<Permanent> oozes = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Ooze"))
                    .toList();
            assertThat(oozes).hasSize(2);

            // Both tokens should have P/T equal to 2 (current slime counter count)
            for (Permanent ooze : oozes) {
                var bonus = gqs.computeStaticBonus(gd, ooze);
                assertThat(ooze.getCard().getPower() + bonus.power()).isEqualTo(2);
                assertThat(ooze.getCard().getToughness() + bonus.toughness()).isEqualTo(2);
            }
        }

        @Test
        @DisplayName("Does not trigger when a token creature dies")
        void doesNotTriggerOnTokenCreatureDeath() {
            harness.addToBattlefield(player1, new GutterGrime());

            // Create a token creature directly
            Card tokenCard = new Card();
            tokenCard.setName("Bear Token");
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setManaCost("");
            tokenCard.setToken(true);
            tokenCard.setColor(CardColor.GREEN);
            tokenCard.setPower(2);
            tokenCard.setToughness(2);
            tokenCard.setSubtypes(List.of(CardSubtype.BEAR));
            Permanent tokenPerm = new Permanent(tokenCard);
            harness.getGameData().playerBattlefields.get(player1.getId()).add(tokenPerm);

            // Use opponent's Wrath so Gutter Grime survives
            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — token dies

            GameData gd = harness.getGameData();

            // No triggers should have fired — stack should be empty
            assertThat(gd.stack).isEmpty();

            // No slime counters
            Permanent grime = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Gutter Grime"))
                    .findFirst().orElse(null);
            assertThat(grime).isNotNull();
            assertThat(grime.getSlimeCounters()).isEqualTo(0);
        }

        @Test
        @DisplayName("Does not trigger when opponent's nontoken creature dies")
        void doesNotTriggerOnOpponentCreatureDeath() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player2, new GrizzlyBears());

            // Use player1's Wrath so Gutter Grime (enchantment) survives, opponent's bear dies
            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — opponent's bear dies

            GameData gd = harness.getGameData();

            // Gutter Grime should have 0 slime counters
            Permanent grime = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Gutter Grime"))
                    .findFirst().orElse(null);
            assertThat(grime).isNotNull();
            assertThat(grime.getSlimeCounters()).isEqualTo(0);
        }

        @Test
        @DisplayName("Token CDA: token has STATIC BoostSelfBySlimeCountersOnLinkedPermanentEffect")
        void tokenHasCorrectStaticEffect() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player1, new GrizzlyBears());

            // Use opponent's Wrath so Gutter Grime survives
            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath
            harness.passBothPriorities(); // Resolve trigger

            GameData gd = harness.getGameData();

            Permanent ooze = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Ooze"))
                    .findFirst().orElse(null);
            assertThat(ooze).isNotNull();

            // The Ooze token should have the linked static effect
            assertThat(ooze.getCard().getEffects(EffectSlot.STATIC)).hasSize(1);
            assertThat(ooze.getCard().getEffects(EffectSlot.STATIC).getFirst())
                    .isInstanceOf(BoostSelfBySlimeCountersOnLinkedPermanentEffect.class);
        }

        @Test
        @DisplayName("Effect fizzles when Gutter Grime leaves the battlefield before trigger resolves")
        void effectFizzlesWhenGutterGrimeDestroyed() {
            harness.addToBattlefield(player1, new GutterGrime());
            harness.addToBattlefield(player1, new GrizzlyBears());

            // Use player1's Wrath — destroys both Gutter Grime (nah, enchantments are safe)
            // Actually Wrath only destroys creatures, so Gutter Grime survives.
            // Let me instead put Gutter Grime on player1 and a bear on player1,
            // then manually remove Gutter Grime before the trigger resolves.

            // Kill the bear
            harness.setHand(player2, List.of(new WrathOfGod()));
            harness.addMana(player2, ManaColor.WHITE, 4);
            harness.forceActivePlayer(player2);

            harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — bear dies, trigger goes on stack

            GameData gd = harness.getGameData();
            assertThat(gd.stack).isNotEmpty();

            // Remove Gutter Grime from battlefield before the trigger resolves
            gd.playerBattlefields.get(player1.getId())
                    .removeIf(p -> p.getCard().getName().equals("Gutter Grime"));

            harness.passBothPriorities(); // Resolve trigger — Gutter Grime is gone

            // No Ooze tokens should exist (effect fizzled)
            List<Permanent> oozes = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Ooze"))
                    .toList();
            assertThat(oozes).isEmpty();
        }
    }
}
