package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.cards.a.Asceticism;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.ConsumeSpirit;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrecursorGolem;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SwordOfBodyAndMind;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.cards.w.WhispersilkCloak;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CopyResolutionServiceTest extends BaseCardTest {

    // =========================================================================
    // resolveCopySpell — CopySpellEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveCopySpell")
    class ResolveCopySpell {

        @Test
        @DisplayName("Copy preserves entry type from target spell")
        void copyPreservesEntryType() {
            CounselOfTheSoratami counsel = new CounselOfTheSoratami();
            harness.setHand(player1, List.of(counsel));
            harness.addMana(player1, ManaColor.BLUE, 3);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castSorcery(player1, 0, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, counsel.getId());

            // Resolve Twincast
            harness.passBothPriorities();

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        }

        @Test
        @DisplayName("Copy preserves effects from target spell")
        void copyPreservesEffects() {
            Shock shock = new Shock();
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

            harness.setHand(player1, List.of(shock));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, bearsPermId);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, shock.getId());

            // Resolve Twincast
            harness.passBothPriorities();

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getEffectsToResolve())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(DealDamageToAnyTargetEffect.class);

            DealDamageToAnyTargetEffect effect = (DealDamageToAnyTargetEffect) copyEntry.getEffectsToResolve().getFirst();
            assertThat(effect.damage()).isEqualTo(2);
        }

        @Test
        @DisplayName("Copy preserves X value from target spell")
        void copyPreservesXValue() {
            ConsumeSpirit consume = new ConsumeSpirit();
            harness.setHand(player1, List.of(consume));
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castSorcery(player1, 0, 3, player2.getId());
            harness.passPriority(player1);
            harness.castInstant(player2, 0, consume.getId());

            // Resolve Twincast
            harness.passBothPriorities();

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getXValue()).isEqualTo(3);
            assertThat(copyEntry.getEffectsToResolve())
                    .first()
                    .isInstanceOf(DealXDamageToAnyTargetAndGainXLifeEffect.class);
        }

        @Test
        @DisplayName("Copy controller is the Twincast caster, not the original spell owner")
        void copyControllerIsTwincastCaster() {
            CounselOfTheSoratami counsel = new CounselOfTheSoratami();
            harness.setHand(player1, List.of(counsel));
            harness.addMana(player1, ManaColor.BLUE, 3);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castSorcery(player1, 0, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, counsel.getId());

            // Resolve Twincast
            harness.passBothPriorities();

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getControllerId()).isEqualTo(player2.getId());
            assertThat(copyEntry.getControllerId()).isNotEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Copy card has a new identity — different UUID from original")
        void copyHasNewCardIdentity() {
            CounselOfTheSoratami counsel = new CounselOfTheSoratami();
            harness.setHand(player1, List.of(counsel));
            harness.addMana(player1, ManaColor.BLUE, 3);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castSorcery(player1, 0, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, counsel.getId());

            // Resolve Twincast
            harness.passBothPriorities();

            StackEntry original = gd.stack.getFirst();
            StackEntry copy = gd.stack.getLast();

            assertThat(copy.getCard().getId()).isNotEqualTo(original.getCard().getId());
            assertThat(copy.getCard().getName()).isEqualTo(original.getCard().getName());
        }

        @Test
        @DisplayName("Copy is marked as copy=true")
        void copyIsMarkedAsCopy() {
            CounselOfTheSoratami counsel = new CounselOfTheSoratami();
            harness.setHand(player1, List.of(counsel));
            harness.addMana(player1, ManaColor.BLUE, 3);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castSorcery(player1, 0, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, counsel.getId());

            // Resolve Twincast
            harness.passBothPriorities();

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.isCopy()).isTrue();

            // The original is NOT a copy
            StackEntry originalEntry = gd.stack.getFirst();
            assertThat(originalEntry.isCopy()).isFalse();
        }

        @Test
        @DisplayName("Copy description is 'Copy of <spell name>'")
        void copyDescriptionPrefixed() {
            Boomerang boomerang = new Boomerang();
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

            harness.setHand(player1, List.of(boomerang));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, bearsPermId);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, boomerang.getId());

            // Resolve Twincast
            harness.passBothPriorities();

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getDescription()).isEqualTo("Copy of Boomerang");
        }

        @Test
        @DisplayName("Copy preserves target from original spell")
        void copyPreservesTarget() {
            Boomerang boomerang = new Boomerang();
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

            harness.setHand(player1, List.of(boomerang));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, bearsPermId);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, boomerang.getId());

            // Resolve Twincast
            harness.passBothPriorities();

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getTargetPermanentId()).isEqualTo(bearsPermId);
        }

        @Test
        @DisplayName("No copy created when target spell was removed from stack")
        void noCopyWhenTargetSpellRemoved() {
            CounselOfTheSoratami counsel = new CounselOfTheSoratami();
            harness.setHand(player1, List.of(counsel));
            harness.addMana(player1, ManaColor.BLUE, 3);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castSorcery(player1, 0, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, counsel.getId());

            // Remove the target spell from stack (simulating counter)
            gd.stack.removeIf(se -> se.getCard().getName().equals("Counsel of the Soratami"));

            // Resolve Twincast — should fizzle, no copy created
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        }

        @Test
        @DisplayName("Game log records copy creation message")
        void gameLogRecordsCopyCreation() {
            CounselOfTheSoratami counsel = new CounselOfTheSoratami();
            harness.setHand(player1, List.of(counsel));
            harness.addMana(player1, ManaColor.BLUE, 3);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castSorcery(player1, 0, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, counsel.getId());

            harness.passBothPriorities();

            assertThat(gd.gameLog).anyMatch(log ->
                    log.contains("copy") && log.contains("Counsel of the Soratami"));
        }

        @Test
        @DisplayName("Copy of X spell resolves with correct X value — deals correct damage")
        void copyOfXSpellResolvesWithCorrectXValue() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            ConsumeSpirit consume = new ConsumeSpirit();
            harness.setHand(player1, List.of(consume));
            harness.addMana(player1, ManaColor.BLACK, 5);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            // Cast Consume Spirit with X=3 targeting player2
            harness.castSorcery(player1, 0, 3, player2.getId());
            harness.passPriority(player1);
            harness.castInstant(player2, 0, consume.getId());

            // Resolve Twincast → copy created
            harness.passBothPriorities();
            // Decline retarget for copy
            harness.handleMayAbilityChosen(player2, false);
            // Resolve copy (controller=player2, target=player2) → 3 dmg to player2, player2 gains 3
            harness.passBothPriorities();
            // Resolve original (controller=player1, target=player2) → 3 dmg to player2, player1 gains 3
            harness.passBothPriorities();

            // Player2 took 3 from copy (net 0 since they also gain 3) + 3 from original = 17
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
            // Player1 gained 3 life from original Consume Spirit
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        }

        @Test
        @DisplayName("Copy of spell ceases to exist — does not go to any graveyard")
        void copyCeasesToExist() {
            CounselOfTheSoratami counsel = new CounselOfTheSoratami();
            harness.setHand(player1, List.of(counsel));
            harness.addMana(player1, ManaColor.BLUE, 3);

            harness.setHand(player2, List.of(new Twincast()));
            harness.addMana(player2, ManaColor.BLUE, 2);

            harness.castSorcery(player1, 0, 0);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, counsel.getId());

            // Resolve Twincast
            harness.passBothPriorities();
            // Resolve copy
            harness.passBothPriorities();
            // Resolve original
            harness.passBothPriorities();

            // Copy of Counsel should not appear in any graveyard
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Counsel of the Soratami") && c != counsel);
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Counsel of the Soratami"));
        }
    }

    // =========================================================================
    // resolveCopyForEachOtherSubtype — CopySpellForEachOtherSubtypePermanentEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveCopyForEachOtherSubtype")
    class ResolveCopyForEachOtherSubtype {

        @Test
        @DisplayName("Creates copies for each other Golem — 2 copies for 3 Golems")
        void createsCopiesForEachOtherGolem() {
            castAndResolveGolemWithTokens();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            UUID golemTokenId = getAnyGolemTokenId(player1);

            harness.castInstant(player1, 0, golemTokenId);
            // Resolve triggered ability → creates 2 copies
            harness.passBothPriorities();

            // Stack: original Shock (bottom) + 2 copies
            assertThat(gd.stack).hasSize(3);
            long copyCount = gd.stack.stream().filter(StackEntry::isCopy).count();
            assertThat(copyCount).isEqualTo(2);
        }

        @Test
        @DisplayName("Each copy targets a different Golem")
        void eachCopyTargetsDifferentGolem() {
            castAndResolveGolemWithTokens();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            UUID golemTokenId = getAnyGolemTokenId(player1);

            harness.castInstant(player1, 0, golemTokenId);
            // Resolve triggered ability
            harness.passBothPriorities();

            List<UUID> allTargets = gd.stack.stream()
                    .map(StackEntry::getTargetPermanentId)
                    .toList();

            assertThat(allTargets).hasSize(3);
            assertThat(allTargets).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("Copies are marked as copy=true")
        void copiesAreMarkedAsCopy() {
            castAndResolveGolemWithTokens();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            UUID golemTokenId = getAnyGolemTokenId(player1);

            harness.castInstant(player1, 0, golemTokenId);
            harness.passBothPriorities();

            // The original Shock is NOT a copy
            StackEntry originalShock = gd.stack.stream()
                    .filter(se -> !se.isCopy())
                    .findFirst().orElseThrow();
            assertThat(originalShock.getCard().getName()).isEqualTo("Shock");

            // All other entries are copies
            gd.stack.stream()
                    .filter(StackEntry::isCopy)
                    .forEach(se -> {
                        assertThat(se.getCard().getName()).isEqualTo("Shock");
                        assertThat(se.isCopy()).isTrue();
                    });
        }

        @Test
        @DisplayName("Copies preserve effects from the original spell")
        void copiesPreserveEffects() {
            castAndResolveGolemWithTokens();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            UUID golemTokenId = getAnyGolemTokenId(player1);

            harness.castInstant(player1, 0, golemTokenId);
            harness.passBothPriorities();

            gd.stack.stream()
                    .filter(StackEntry::isCopy)
                    .forEach(se -> {
                        assertThat(se.getEffectsToResolve()).hasSize(1);
                        assertThat(se.getEffectsToResolve().getFirst())
                                .isInstanceOf(DealDamageToAnyTargetEffect.class);
                    });
        }

        @Test
        @DisplayName("Game log records a copy creation for each eligible Golem")
        void gameLogRecordsCopiesCreated() {
            castAndResolveGolemWithTokens();
            harness.clearMessages();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            UUID golemTokenId = getAnyGolemTokenId(player1);

            harness.castInstant(player1, 0, golemTokenId);
            harness.passBothPriorities();

            long copyLogCount = gd.gameLog.stream()
                    .filter(log -> log.contains("copy") && log.contains("Shock"))
                    .count();
            assertThat(copyLogCount).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("Shrouded Golem is skipped — no copy targets it")
        void shroudedGolemIsSkipped() {
            castAndResolveGolemWithTokens();

            // Equip WhispersilkCloak on one Golem token to grant shroud
            UUID shroudedGolemId = getAnyGolemTokenId(player1);
            Permanent cloak = new Permanent(new WhispersilkCloak());
            cloak.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(cloak);
            cloak.setAttachedTo(shroudedGolemId);

            // Target a different Golem with Shock
            UUID targetGolemId = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOLEM))
                    .filter(p -> !p.getId().equals(shroudedGolemId))
                    .findFirst().orElseThrow().getId();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, targetGolemId);
            harness.passBothPriorities(); // resolve triggered ability

            // Only 1 copy should be created (for the non-shrouded, non-targeted Golem)
            long copyCount = gd.stack.stream().filter(StackEntry::isCopy).count();
            assertThat(copyCount).isEqualTo(1);

            // The shrouded Golem should NOT be targeted by any stack entry
            assertThat(gd.stack).noneMatch(se -> se.getTargetPermanentId().equals(shroudedGolemId));
        }

        @Test
        @DisplayName("Golem with protection from spell color is skipped")
        void protectionFromSpellColorSkipsGolem() {
            castAndResolveGolemWithTokens();

            // Equip Sword of Body and Mind on one Golem token (grants protection from green and blue)
            UUID protectedGolemId = getAnyGolemTokenId(player1);
            Permanent sword = new Permanent(new SwordOfBodyAndMind());
            sword.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(sword);
            sword.setAttachedTo(protectedGolemId);

            // Target a different Golem with Boomerang (blue spell)
            UUID targetGolemId = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOLEM))
                    .filter(p -> !p.getId().equals(protectedGolemId))
                    .findFirst().orElseThrow().getId();

            Boomerang boomerang = new Boomerang();
            harness.setHand(player1, List.of(boomerang));
            harness.addMana(player1, ManaColor.BLUE, 2);

            harness.castInstant(player1, 0, targetGolemId);
            harness.passBothPriorities(); // resolve triggered ability

            // Only 1 copy should be created (the protected Golem is skipped)
            long copyCount = gd.stack.stream().filter(StackEntry::isCopy).count();
            assertThat(copyCount).isEqualTo(1);

            // The protected Golem should NOT be targeted
            assertThat(gd.stack).noneMatch(se -> se.getTargetPermanentId().equals(protectedGolemId));
        }

        @Test
        @DisplayName("Opponent's Golem with CantBeTargetOfSpellsOrAbilities is skipped")
        void opponentGolemWithCantBeTargetedIsSkipped() {
            // Player1 has PrecursorGolem + 2 tokens = 3 Golems
            castAndResolveGolemWithTokens();

            // Player2 also has a Golem creature manually added
            Permanent opponentGolem = addGolemPermanent(player2);

            // Player2 has Asceticism — grants CantBeTargetOfSpellsOrAbilities to player2's creatures
            harness.addToBattlefield(player2, new Asceticism());

            UUID targetGolemId = getAnyGolemTokenId(player1);

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, targetGolemId);
            harness.passBothPriorities(); // resolve triggered ability

            // Copies should only target player1's Golems, NOT player2's protected Golem
            assertThat(gd.stack).noneMatch(se ->
                    se.getTargetPermanentId() != null && se.getTargetPermanentId().equals(opponentGolem.getId()));

            // 2 copies created for player1's other 2 Golems
            long copyCount = gd.stack.stream().filter(StackEntry::isCopy).count();
            assertThat(copyCount).isEqualTo(2);
        }

        @Test
        @DisplayName("No trigger when spell targets a non-Golem creature")
        void noTriggerWhenTargetingNonGolem() {
            castAndResolveGolemWithTokens();

            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, bearsId);

            // Only the original Shock on the stack — no trigger
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        }

        @Test
        @DisplayName("No trigger when spell targets a player")
        void noTriggerWhenTargetingPlayer() {
            castAndResolveGolemWithTokens();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);

            harness.castInstant(player1, 0, player2.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        }

        @Test
        @DisplayName("Copies controller matches the casting player of the original spell")
        void copiesControllerMatchesCastingPlayer() {
            castAndResolveGolemWithTokens();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            UUID golemTokenId = getAnyGolemTokenId(player1);

            harness.castInstant(player1, 0, golemTokenId);
            harness.passBothPriorities();

            gd.stack.stream()
                    .filter(StackEntry::isCopy)
                    .forEach(se -> assertThat(se.getControllerId()).isEqualTo(player1.getId()));
        }

        @Test
        @DisplayName("All copies and original resolve — all 3/3 Golems survive 2 damage")
        void allCopiesResolveAndGolemsSurvive() {
            castAndResolveGolemWithTokens();

            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            UUID golemTokenId = getAnyGolemTokenId(player1);

            harness.castInstant(player1, 0, golemTokenId);
            harness.passBothPriorities(); // resolve triggered ability
            harness.passBothPriorities(); // resolve first copy
            harness.passBothPriorities(); // resolve second copy
            harness.passBothPriorities(); // resolve original Shock

            assertThat(gd.stack).isEmpty();
            // All 3 Golems survive (2 damage < 3 toughness)
            long golemCount = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOLEM))
                    .count();
            assertThat(golemCount).isEqualTo(3);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void castAndResolveGolemWithTokens() {
        harness.setHand(player1, List.of(new PrecursorGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private UUID getAnyGolemTokenId(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Golem"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Golem token found"))
                .getId();
    }

    private Permanent addGolemPermanent(Player player) {
        GrizzlyBears golemCard = new GrizzlyBears();
        golemCard.setName("Golem");
        golemCard.setSubtypes(List.of(CardSubtype.GOLEM));
        Permanent perm = new Permanent(golemCard);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
