package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithXChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StackResolutionServiceTest extends BaseCardTest {

    private StackResolutionService svc() {
        return harness.getStackResolutionService();
    }

    private Card createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("1G");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card createEnchantment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost("1W");
        return card;
    }

    private Card createAura(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.AURA));
        card.setManaCost("1W");
        return card;
    }

    private Card createArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("2");
        return card;
    }

    private Card createPlaneswalker(String name, int loyalty) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.PLANESWALKER);
        card.setManaCost("3U");
        card.setLoyalty(loyalty);
        return card;
    }

    private Card createInstant(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("R");
        return card;
    }

    private Card createSorcery(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.SORCERY);
        card.setManaCost("1B");
        return card;
    }

    @Nested
    @DisplayName("resolveTopOfStack basics")
    class ResolveTopOfStack {

        @Test
        @DisplayName("Does nothing when the stack is empty")
        void doesNothingWhenStackEmpty() {
            gd.stack.clear();

            svc().resolveTopOfStack(gd);

            // No exception, battlefield unchanged
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Clears priorityPassedBy after resolution")
        void clearsPriorityPassedBy() {
            Card card = createCreature("Test Creature");
            gd.stack.addLast(new StackEntry(card, player1.getId()));
            gd.priorityPassedBy.add(player1.getId());
            gd.priorityPassedBy.add(player2.getId());

            svc().resolveTopOfStack(gd);

            assertThat(gd.priorityPassedBy).isEmpty();
        }

        @Test
        @DisplayName("Resolves the top (last) entry from the stack")
        void resolvesTopEntry() {
            Card first = createCreature("First Creature");
            Card second = createCreature("Second Creature");
            gd.stack.addLast(new StackEntry(first, player1.getId()));
            gd.stack.addLast(new StackEntry(second, player1.getId()));

            svc().resolveTopOfStack(gd);

            // Second creature (top of stack) should have entered the battlefield
            harness.assertOnBattlefield(player1, "Second Creature");
            // First creature should still be on the stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("First Creature");
        }
    }

    @Nested
    @DisplayName("Creature spell resolution")
    class CreatureSpellResolution {

        @Test
        @DisplayName("Creature enters the battlefield under controller's control")
        void creatureEntersBattlefield() {
            Card card = createCreature("Test Creature");
            gd.stack.addLast(new StackEntry(card, player1.getId()));

            svc().resolveTopOfStack(gd);

            harness.assertOnBattlefield(player1, "Test Creature");
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Creature enters under player 2's control when they cast it")
        void creatureEntersForCorrectPlayer() {
            Card card = createCreature("P2 Creature");
            gd.stack.addLast(new StackEntry(card, player2.getId()));

            svc().resolveTopOfStack(gd);

            harness.assertOnBattlefield(player2, "P2 Creature");
            harness.assertNotOnBattlefield(player1, "P2 Creature");
        }
    }

    @Nested
    @DisplayName("Enchantment spell resolution")
    class EnchantmentSpellResolution {

        @Test
        @DisplayName("Non-aura enchantment enters the battlefield")
        void nonAuraEntersBattlefield() {
            Card card = createEnchantment("Test Enchantment");
            StackEntry entry = new StackEntry(StackEntryType.ENCHANTMENT_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertOnBattlefield(player1, "Test Enchantment");
        }

        @Test
        @DisplayName("Aura attaches to its target permanent")
        void auraAttachesToTarget() {
            Card target = createCreature("Target Creature");
            harness.addToBattlefield(player2, target);
            UUID targetId = harness.getPermanentId(player2, "Target Creature");

            Card aura = createAura("Test Aura");
            StackEntry entry = new StackEntry(StackEntryType.ENCHANTMENT_SPELL, aura,
                    player1.getId(), aura.getName(), List.of(), 0, targetId, null);
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertOnBattlefield(player1, "Test Aura");
            Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Test Aura"))
                    .findFirst().orElseThrow();
            assertThat(auraPerm.getAttachedTo()).isEqualTo(targetId);
        }

        @Test
        @DisplayName("Aura fizzles when target is no longer on the battlefield")
        void auraFizzlesWhenTargetGone() {
            UUID removedTargetId = UUID.randomUUID();

            Card aura = createAura("Fizzle Aura");
            StackEntry entry = new StackEntry(StackEntryType.ENCHANTMENT_SPELL, aura,
                    player1.getId(), aura.getName(), List.of(), 0, removedTargetId, null);
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertNotOnBattlefield(player1, "Fizzle Aura");
            harness.assertInGraveyard(player1, "Fizzle Aura");
        }
    }

    @Nested
    @DisplayName("Artifact spell resolution")
    class ArtifactSpellResolution {

        @Test
        @DisplayName("Artifact enters the battlefield")
        void artifactEntersBattlefield() {
            Card card = createArtifact("Test Artifact");
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertOnBattlefield(player1, "Test Artifact");
        }

        @Test
        @DisplayName("Artifact enters with X charge counters")
        void artifactEntersWithXChargeCounters() {
            Card card = createArtifact("X Counter Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithXChargeCountersEffect());
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    player1.getId(), card.getName(), List.of(), 5);
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertOnBattlefield(player1, "X Counter Artifact");
            Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("X Counter Artifact"))
                    .findFirst().orElseThrow();
            assertThat(perm.getChargeCounters()).isEqualTo(5);
        }

        @Test
        @DisplayName("Artifact enters with fixed charge counters")
        void artifactEntersWithFixedChargeCounters() {
            Card card = createArtifact("Fixed Counter Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(3));
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertOnBattlefield(player1, "Fixed Counter Artifact");
            Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Fixed Counter Artifact"))
                    .findFirst().orElseThrow();
            assertThat(perm.getChargeCounters()).isEqualTo(3);
        }

        @Test
        @DisplayName("CantHaveCountersEffect prevents X charge counters")
        void cantHaveCountersPreventsXChargeCounters() {
            Card card = createArtifact("No Counter Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithXChargeCountersEffect());
            card.addEffect(EffectSlot.STATIC, new CantHaveCountersEffect());
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    player1.getId(), card.getName(), List.of(), 5);
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertOnBattlefield(player1, "No Counter Artifact");
            Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("No Counter Artifact"))
                    .findFirst().orElseThrow();
            assertThat(perm.getChargeCounters()).isZero();
        }

        @Test
        @DisplayName("CantHaveCountersEffect prevents fixed charge counters")
        void cantHaveCountersPreventsFixedChargeCounters() {
            Card card = createArtifact("No Fixed Counter Artifact");
            card.addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnterWithFixedChargeCountersEffect(4));
            card.addEffect(EffectSlot.STATIC, new CantHaveCountersEffect());
            StackEntry entry = new StackEntry(StackEntryType.ARTIFACT_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("No Fixed Counter Artifact"))
                    .findFirst().orElseThrow();
            assertThat(perm.getChargeCounters()).isZero();
        }
    }

    @Nested
    @DisplayName("Planeswalker spell resolution")
    class PlaneswalkerSpellResolution {

        @Test
        @DisplayName("Planeswalker enters with correct loyalty counters")
        void planeswalkerEntersWithLoyalty() {
            Card card = createPlaneswalker("Test Planeswalker", 4);
            StackEntry entry = new StackEntry(StackEntryType.PLANESWALKER_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertOnBattlefield(player1, "Test Planeswalker");
            Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Test Planeswalker"))
                    .findFirst().orElseThrow();
            assertThat(perm.getLoyaltyCounters()).isEqualTo(4);
        }

        @Test
        @DisplayName("Planeswalker is not summoning sick")
        void planeswalkerNotSummoningSick() {
            Card card = createPlaneswalker("Active Planeswalker", 3);
            StackEntry entry = new StackEntry(StackEntryType.PLANESWALKER_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Active Planeswalker"))
                    .findFirst().orElseThrow();
            assertThat(perm.isSummoningSick()).isFalse();
        }

        @Test
        @DisplayName("Planeswalker with null loyalty enters with 0 loyalty counters")
        void planeswalkerNullLoyaltyDefaultsToZero() {
            Card card = createPlaneswalker("Zero PW", 0);
            card.setLoyalty(null);
            StackEntry entry = new StackEntry(StackEntryType.PLANESWALKER_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            // Planeswalker with 0 loyalty will be destroyed by SBA, so check graveyard
            harness.assertInGraveyard(player1, "Zero PW");
        }
    }

    @Nested
    @DisplayName("Spell and ability resolution")
    class SpellAndAbilityResolution {

        @Test
        @DisplayName("Instant resolves and goes to graveyard")
        void instantGoesToGraveyard() {
            Card card = createInstant("Test Instant");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertInGraveyard(player1, "Test Instant");
        }

        @Test
        @DisplayName("Sorcery resolves and goes to graveyard")
        void sorceryGoesToGraveyard() {
            Card card = createSorcery("Test Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertInGraveyard(player1, "Test Sorcery");
        }

        @Test
        @DisplayName("Targeted spell fizzles when target is gone and goes to graveyard")
        void targetedSpellFizzlesGoesToGraveyard() {
            UUID nonExistentTarget = UUID.randomUUID();
            Card card = createInstant("Fizzle Instant");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    player1.getId(), card.getName(), List.of(), 0, nonExistentTarget, null);
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertInGraveyard(player1, "Fizzle Instant");
        }

        @Test
        @DisplayName("Fizzled copy does not go to graveyard (ceases to exist)")
        void fizzledCopyCeasesToExist() {
            UUID nonExistentTarget = UUID.randomUUID();
            Card card = createInstant("Copy Fizzle");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    player1.getId(), card.getName(), List.of(), 0, nonExistentTarget, null);
            entry.setCopy(true);
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertNotInGraveyard(player1, "Copy Fizzle");
        }

        @Test
        @DisplayName("Triggered ability that fizzles does not go to graveyard")
        void triggeredAbilityFizzleNoGraveyard() {
            UUID nonExistentTarget = UUID.randomUUID();
            Card card = createCreature("Trigger Source");
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card,
                    player1.getId(), "Trigger Source's ability", List.of(), 0, nonExistentTarget, null);
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            // Abilities don't go to graveyard — only spells do
            harness.assertNotInGraveyard(player1, "Trigger Source");
        }

        @Test
        @DisplayName("ExileSpellEffect causes spell to be exiled instead of going to graveyard")
        void exileSpellEffectExilesSpell() {
            Card card = createInstant("Exile Instant");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    player1.getId(), card.getName(), List.of(new ExileSpellEffect()));
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Exile Instant"));
            harness.assertNotInGraveyard(player1, "Exile Instant");
        }

        @Test
        @DisplayName("ReturnToHandAfterResolving sends spell back to hand")
        void returnToHandAfterResolving() {
            Card card = createSorcery("Buyback Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            entry.setReturnToHandAfterResolving(true);
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertInHand(player1, "Buyback Sorcery");
            harness.assertNotInGraveyard(player1, "Buyback Sorcery");
        }

        @Test
        @DisplayName("ShuffleIntoLibraryEffect shuffles spell into library")
        void shuffleIntoLibraryEffect() {
            Card card = createSorcery("Shuffle Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    player1.getId(), card.getName(), List.of(new ShuffleIntoLibraryEffect()));
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            assertThat(gd.playerDecks.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Shuffle Sorcery"));
            harness.assertNotInGraveyard(player1, "Shuffle Sorcery");
        }

        @Test
        @DisplayName("Copy of a spell ceases to exist (does not go to graveyard)")
        void copyCeasesToExist() {
            Card card = createInstant("Copied Spell");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            entry.setCopy(true);
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertNotInGraveyard(player1, "Copied Spell");
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Copied Spell"));
        }

        @Test
        @DisplayName("Activated ability resolves without going to graveyard")
        void activatedAbilityDoesNotGoToGraveyard() {
            Card card = createCreature("Ability Source");
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, card,
                    player1.getId(), "Ability Source's ability", List.of());
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            harness.assertNotInGraveyard(player1, "Ability Source");
        }

        @Test
        @DisplayName("End turn requested exiles the resolving spell")
        void endTurnRequestedExilesSpell() {
            Card card = createSorcery("End Turn Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            gd.stack.addLast(entry);
            gd.endTurnRequested = true;

            svc().resolveTopOfStack(gd);

            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("End Turn Sorcery"));
            harness.assertNotInGraveyard(player1, "End Turn Sorcery");
            assertThat(gd.endTurnRequested).isFalse();
        }

        @Test
        @DisplayName("End turn requested with a copy does not exile (ceases to exist)")
        void endTurnRequestedCopyCeasesToExist() {
            Card card = createInstant("End Turn Copy");
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card,
                    player1.getId(), card.getName(), List.of());
            entry.setCopy(true);
            gd.stack.addLast(entry);
            gd.endTurnRequested = true;

            svc().resolveTopOfStack(gd);

            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("End Turn Copy"));
            harness.assertNotInGraveyard(player1, "End Turn Copy");
        }

        @Test
        @DisplayName("Non-targeting spell does not fizzle even without valid target")
        void nonTargetingSpellDoesNotFizzle() {
            UUID nonExistentTarget = UUID.randomUUID();
            Card card = createSorcery("Non-Targeting Sorcery");
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card,
                    player1.getId(), card.getName(), List.of(), 0, nonExistentTarget, null);
            entry.setNonTargeting(true);
            gd.stack.addLast(entry);

            svc().resolveTopOfStack(gd);

            // Spell resolved normally and goes to graveyard (not fizzled)
            harness.assertInGraveyard(player1, "Non-Targeting Sorcery");
        }
    }
}
