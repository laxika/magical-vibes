package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CopyResolutionServiceTest {

    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private ValidTargetService validTargetService;
    @Mock private GameQueryService gameQueryService;

    @InjectMocks private CopyResolutionService service;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // ===== Helper methods =====

    private Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    private Card createSpellCard(String name, List<CardEffect> effects) {
        Card card = createCard(name);
        card.setType(CardType.INSTANT);
        return card;
    }

    private StackEntry spellEntry(Card card, UUID controllerId, StackEntryType type,
                                  List<CardEffect> effects, int xValue, UUID targetId) {
        return new StackEntry(type, card, controllerId, card.getName(), effects, xValue,
                targetId, null, null, null, null, null);
    }

    private StackEntry spellEntry(Card card, UUID controllerId, StackEntryType type,
                                  List<CardEffect> effects, UUID targetId) {
        return spellEntry(card, controllerId, type, effects, 0, targetId);
    }

    private StackEntry copySpellTriggerEntry(Card twincastCard, UUID controllerId, UUID targetCardId) {
        StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, twincastCard, controllerId,
                twincastCard.getName(), List.of(new CopySpellEffect()), 0,
                targetCardId, null, null, null, null, null);
        return entry;
    }

    private Permanent createCreaturePermanent(String name, List<CardSubtype> subtypes) {
        Card card = createCard(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(subtypes);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        return perm;
    }

    // =========================================================================
    // resolveCopySpell — CopySpellEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveCopySpell")
    class ResolveCopySpell {

        @Test
        @DisplayName("Copy preserves entry type from target spell")
        void copyPreservesEntryType() {
            Card counselCard = createSpellCard("Counsel of the Soratami", List.of());
            StackEntry targetEntry = spellEntry(counselCard, player1Id, StackEntryType.SORCERY_SPELL,
                    List.of(), null);
            gd.stack.add(targetEntry);

            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, counselCard.getId());

            service.resolveCopySpell(gd, twincastEntry);

            assertThat(gd.stack).hasSize(2);
            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        }

        @Test
        @DisplayName("Copy preserves effects from target spell")
        void copyPreservesEffects() {
            DealDamageToAnyTargetEffect damageEffect = new DealDamageToAnyTargetEffect(2);
            Card shockCard = createSpellCard("Shock", List.of(damageEffect));
            UUID bearsPermId = UUID.randomUUID();
            StackEntry targetEntry = spellEntry(shockCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(damageEffect), bearsPermId);
            gd.stack.add(targetEntry);

            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, shockCard.getId());

            service.resolveCopySpell(gd, twincastEntry);

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getEffectsToResolve())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(DealDamageToAnyTargetEffect.class);

            DealDamageToAnyTargetEffect copiedEffect = (DealDamageToAnyTargetEffect) copyEntry.getEffectsToResolve().getFirst();
            assertThat(copiedEffect.damage()).isEqualTo(2);
        }

        @Test
        @DisplayName("Copy preserves X value from target spell")
        void copyPreservesXValue() {
            Card consumeCard = createSpellCard("Consume Spirit", List.of());
            StackEntry targetEntry = spellEntry(consumeCard, player1Id, StackEntryType.SORCERY_SPELL,
                    List.of(), 3, player2Id);
            gd.stack.add(targetEntry);

            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, consumeCard.getId());

            service.resolveCopySpell(gd, twincastEntry);

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getXValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("Copy controller is the Twincast caster, not the original spell owner")
        void copyControllerIsTwincastCaster() {
            Card counselCard = createSpellCard("Counsel of the Soratami", List.of());
            StackEntry targetEntry = spellEntry(counselCard, player1Id, StackEntryType.SORCERY_SPELL,
                    List.of(), null);
            gd.stack.add(targetEntry);

            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, counselCard.getId());

            service.resolveCopySpell(gd, twincastEntry);

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getControllerId()).isEqualTo(player2Id);
            assertThat(copyEntry.getControllerId()).isNotEqualTo(player1Id);
        }

        @Test
        @DisplayName("Copy card has a new identity — different UUID from original")
        void copyHasNewCardIdentity() {
            Card counselCard = createSpellCard("Counsel of the Soratami", List.of());
            StackEntry targetEntry = spellEntry(counselCard, player1Id, StackEntryType.SORCERY_SPELL,
                    List.of(), null);
            gd.stack.add(targetEntry);

            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, counselCard.getId());

            service.resolveCopySpell(gd, twincastEntry);

            StackEntry original = gd.stack.getFirst();
            StackEntry copy = gd.stack.getLast();

            assertThat(copy.getCard().getId()).isNotEqualTo(original.getCard().getId());
            assertThat(copy.getCard().getName()).isEqualTo(original.getCard().getName());
        }

        @Test
        @DisplayName("Copy is marked as copy=true")
        void copyIsMarkedAsCopy() {
            Card counselCard = createSpellCard("Counsel of the Soratami", List.of());
            StackEntry targetEntry = spellEntry(counselCard, player1Id, StackEntryType.SORCERY_SPELL,
                    List.of(), null);
            gd.stack.add(targetEntry);

            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, counselCard.getId());

            service.resolveCopySpell(gd, twincastEntry);

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.isCopy()).isTrue();

            // The original is NOT a copy
            StackEntry originalEntry = gd.stack.getFirst();
            assertThat(originalEntry.isCopy()).isFalse();
        }

        @Test
        @DisplayName("Copy description is 'Copy of <spell name>'")
        void copyDescriptionPrefixed() {
            Card boomerangCard = createSpellCard("Boomerang", List.of());
            UUID bearsPermId = UUID.randomUUID();
            StackEntry targetEntry = spellEntry(boomerangCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(new ReturnTargetPermanentToHandEffect()), bearsPermId);
            gd.stack.add(targetEntry);

            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, boomerangCard.getId());

            service.resolveCopySpell(gd, twincastEntry);

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getDescription()).isEqualTo("Copy of Boomerang");
        }

        @Test
        @DisplayName("Copy preserves target from original spell")
        void copyPreservesTarget() {
            Card boomerangCard = createSpellCard("Boomerang", List.of());
            UUID bearsPermId = UUID.randomUUID();
            StackEntry targetEntry = spellEntry(boomerangCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(new ReturnTargetPermanentToHandEffect()), bearsPermId);
            gd.stack.add(targetEntry);

            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, boomerangCard.getId());

            service.resolveCopySpell(gd, twincastEntry);

            StackEntry copyEntry = gd.stack.getLast();
            assertThat(copyEntry.getTargetId()).isEqualTo(bearsPermId);
        }

        @Test
        @DisplayName("No copy created when target spell was removed from stack")
        void noCopyWhenTargetSpellRemoved() {
            // Stack is empty — target spell already removed
            Card twincastCard = createCard("Twincast");
            UUID removedCardId = UUID.randomUUID();
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, removedCardId);

            service.resolveCopySpell(gd, twincastEntry);

            assertThat(gd.stack).isEmpty();
            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Game log records copy creation message")
        void gameLogRecordsCopyCreation() {
            Card counselCard = createSpellCard("Counsel of the Soratami", List.of());
            StackEntry targetEntry = spellEntry(counselCard, player1Id, StackEntryType.SORCERY_SPELL,
                    List.of(), null);
            gd.stack.add(targetEntry);

            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, counselCard.getId());

            service.resolveCopySpell(gd, twincastEntry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("A copy of Counsel of the Soratami is created."));
        }

        @Test
        @DisplayName("Does nothing when targetId is null")
        void doesNothingWhenTargetIdIsNull() {
            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = new StackEntry(StackEntryType.INSTANT_SPELL, twincastCard, player2Id,
                    twincastCard.getName(), List.of(new CopySpellEffect()), 0,
                    (UUID) null, null, null, null, null, null);

            service.resolveCopySpell(gd, twincastEntry);

            assertThat(gd.stack).isEmpty();
            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Queues a retarget PendingMayAbility when the copy has a target")
        void queuesRetargetMayAbilityWhenCopyHasTarget() {
            Card boomerangCard = createSpellCard("Boomerang", List.of());
            UUID bearsPermId = UUID.randomUUID();
            StackEntry targetEntry = spellEntry(boomerangCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(new ReturnTargetPermanentToHandEffect()), bearsPermId);
            gd.stack.add(targetEntry);

            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, boomerangCard.getId());

            service.resolveCopySpell(gd, twincastEntry);

            assertThat(gd.pendingMayAbilities).hasSize(1);
            PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
            assertThat(ability.controllerId()).isEqualTo(player2Id);
            assertThat(ability.description()).contains("Choose new targets");
            assertThat(ability.description()).contains("Boomerang");
        }

        @Test
        @DisplayName("No retarget PendingMayAbility when copy has no target")
        void noRetargetMayAbilityWhenCopyHasNoTarget() {
            Card counselCard = createSpellCard("Counsel of the Soratami", List.of());
            StackEntry targetEntry = spellEntry(counselCard, player1Id, StackEntryType.SORCERY_SPELL,
                    List.of(), null);
            gd.stack.add(targetEntry);

            Card twincastCard = createCard("Twincast");
            StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, counselCard.getId());

            service.resolveCopySpell(gd, twincastEntry);

            assertThat(gd.pendingMayAbilities).isEmpty();
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
            // Set up 3 Golem permanents on player1's battlefield
            Permanent golem1 = createCreaturePermanent("Precursor Golem", List.of(CardSubtype.GOLEM));
            Permanent golem2 = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            Permanent golem3 = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            gd.playerBattlefields.get(player1Id).addAll(List.of(golem1, golem2, golem3));

            UUID originalTargetId = golem1.getId();

            // Create spell snapshot (Shock targeting golem1)
            DealDamageToAnyTargetEffect damageEffect = new DealDamageToAnyTargetEffect(2);
            Card shockCard = createSpellCard("Shock", List.of(damageEffect));
            StackEntry spellSnapshot = spellEntry(shockCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(damageEffect), originalTargetId);

            CopySpellForEachOtherSubtypePermanentEffect effect =
                    new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM, spellSnapshot, player1Id, originalTargetId);

            Card triggerCard = createCard("Precursor Golem");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Precursor Golem trigger", List.of(effect));

            when(validTargetService.canPermanentBeTargetedBySpell(eq(gd), any(), eq(shockCard), eq(player1Id)))
                    .thenReturn(true);

            service.resolveCopyForEachOtherSubtype(gd, triggerEntry, effect);

            // 2 copies added to the stack (one for each non-targeted Golem)
            assertThat(gd.stack).hasSize(2);
            assertThat(gd.stack).allMatch(StackEntry::isCopy);
        }

        @Test
        @DisplayName("Each copy targets a different Golem")
        void eachCopyTargetsDifferentGolem() {
            Permanent golem1 = createCreaturePermanent("Precursor Golem", List.of(CardSubtype.GOLEM));
            Permanent golem2 = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            Permanent golem3 = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            gd.playerBattlefields.get(player1Id).addAll(List.of(golem1, golem2, golem3));

            UUID originalTargetId = golem1.getId();
            DealDamageToAnyTargetEffect damageEffect = new DealDamageToAnyTargetEffect(2);
            Card shockCard = createSpellCard("Shock", List.of(damageEffect));
            StackEntry spellSnapshot = spellEntry(shockCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(damageEffect), originalTargetId);

            CopySpellForEachOtherSubtypePermanentEffect effect =
                    new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM, spellSnapshot, player1Id, originalTargetId);

            Card triggerCard = createCard("Precursor Golem");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Precursor Golem trigger", List.of(effect));

            when(validTargetService.canPermanentBeTargetedBySpell(eq(gd), any(), eq(shockCard), eq(player1Id)))
                    .thenReturn(true);

            service.resolveCopyForEachOtherSubtype(gd, triggerEntry, effect);

            List<UUID> copyTargets = gd.stack.stream()
                    .map(StackEntry::getTargetId)
                    .toList();

            assertThat(copyTargets).hasSize(2);
            assertThat(copyTargets).doesNotHaveDuplicates();
            assertThat(copyTargets).containsExactlyInAnyOrder(golem2.getId(), golem3.getId());
        }

        @Test
        @DisplayName("Copies are marked as copy=true")
        void copiesAreMarkedAsCopy() {
            Permanent golem1 = createCreaturePermanent("Precursor Golem", List.of(CardSubtype.GOLEM));
            Permanent golem2 = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            gd.playerBattlefields.get(player1Id).addAll(List.of(golem1, golem2));

            UUID originalTargetId = golem1.getId();
            DealDamageToAnyTargetEffect damageEffect = new DealDamageToAnyTargetEffect(2);
            Card shockCard = createSpellCard("Shock", List.of(damageEffect));
            StackEntry spellSnapshot = spellEntry(shockCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(damageEffect), originalTargetId);

            CopySpellForEachOtherSubtypePermanentEffect effect =
                    new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM, spellSnapshot, player1Id, originalTargetId);

            Card triggerCard = createCard("Precursor Golem");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Precursor Golem trigger", List.of(effect));

            when(validTargetService.canPermanentBeTargetedBySpell(eq(gd), any(), eq(shockCard), eq(player1Id)))
                    .thenReturn(true);

            service.resolveCopyForEachOtherSubtype(gd, triggerEntry, effect);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().isCopy()).isTrue();
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
        }

        @Test
        @DisplayName("Copies preserve effects from the original spell")
        void copiesPreserveEffects() {
            Permanent golem1 = createCreaturePermanent("Precursor Golem", List.of(CardSubtype.GOLEM));
            Permanent golem2 = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            gd.playerBattlefields.get(player1Id).addAll(List.of(golem1, golem2));

            UUID originalTargetId = golem1.getId();
            DealDamageToAnyTargetEffect damageEffect = new DealDamageToAnyTargetEffect(2);
            Card shockCard = createSpellCard("Shock", List.of(damageEffect));
            StackEntry spellSnapshot = spellEntry(shockCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(damageEffect), originalTargetId);

            CopySpellForEachOtherSubtypePermanentEffect effect =
                    new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM, spellSnapshot, player1Id, originalTargetId);

            Card triggerCard = createCard("Precursor Golem");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Precursor Golem trigger", List.of(effect));

            when(validTargetService.canPermanentBeTargetedBySpell(eq(gd), any(), eq(shockCard), eq(player1Id)))
                    .thenReturn(true);

            service.resolveCopyForEachOtherSubtype(gd, triggerEntry, effect);

            gd.stack.forEach(se -> {
                assertThat(se.getEffectsToResolve()).hasSize(1);
                assertThat(se.getEffectsToResolve().getFirst())
                        .isInstanceOf(DealDamageToAnyTargetEffect.class);
            });
        }

        @Test
        @DisplayName("Game log records a copy creation for each eligible Golem")
        void gameLogRecordsCopiesCreated() {
            Permanent golem1 = createCreaturePermanent("Precursor Golem", List.of(CardSubtype.GOLEM));
            Permanent golem2 = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            Permanent golem3 = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            gd.playerBattlefields.get(player1Id).addAll(List.of(golem1, golem2, golem3));

            UUID originalTargetId = golem1.getId();
            DealDamageToAnyTargetEffect damageEffect = new DealDamageToAnyTargetEffect(2);
            Card shockCard = createSpellCard("Shock", List.of(damageEffect));
            StackEntry spellSnapshot = spellEntry(shockCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(damageEffect), originalTargetId);

            CopySpellForEachOtherSubtypePermanentEffect effect =
                    new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM, spellSnapshot, player1Id, originalTargetId);

            Card triggerCard = createCard("Precursor Golem");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Precursor Golem trigger", List.of(effect));

            when(validTargetService.canPermanentBeTargetedBySpell(eq(gd), any(), eq(shockCard), eq(player1Id)))
                    .thenReturn(true);

            service.resolveCopyForEachOtherSubtype(gd, triggerEntry, effect);

            verify(gameBroadcastService, times(2)).logAndBroadcast(eq(gd),
                    eq("A copy of Shock is created targeting Golem."));
        }

        @Test
        @DisplayName("Untargetable Golem is skipped — no copy targets it")
        void untargetableGolemIsSkipped() {
            Permanent golem1 = createCreaturePermanent("Precursor Golem", List.of(CardSubtype.GOLEM));
            Permanent golem2 = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            Permanent shroudedGolem = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            gd.playerBattlefields.get(player1Id).addAll(List.of(golem1, golem2, shroudedGolem));

            UUID originalTargetId = golem1.getId();
            DealDamageToAnyTargetEffect damageEffect = new DealDamageToAnyTargetEffect(2);
            Card shockCard = createSpellCard("Shock", List.of(damageEffect));
            StackEntry spellSnapshot = spellEntry(shockCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(damageEffect), originalTargetId);

            CopySpellForEachOtherSubtypePermanentEffect effect =
                    new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM, spellSnapshot, player1Id, originalTargetId);

            Card triggerCard = createCard("Precursor Golem");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Precursor Golem trigger", List.of(effect));

            // golem2 is targetable, shroudedGolem is NOT
            when(validTargetService.canPermanentBeTargetedBySpell(gd, golem2, shockCard, player1Id))
                    .thenReturn(true);
            when(validTargetService.canPermanentBeTargetedBySpell(gd, shroudedGolem, shockCard, player1Id))
                    .thenReturn(false);

            service.resolveCopyForEachOtherSubtype(gd, triggerEntry, effect);

            // Only 1 copy created (for golem2)
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(golem2.getId());
        }

        @Test
        @DisplayName("Non-Golem permanent is not considered for copies")
        void nonGolemPermanentIgnored() {
            Permanent golem1 = createCreaturePermanent("Precursor Golem", List.of(CardSubtype.GOLEM));
            Permanent bear = createCreaturePermanent("Grizzly Bears", List.of(CardSubtype.BEAR));
            gd.playerBattlefields.get(player1Id).addAll(List.of(golem1, bear));

            UUID originalTargetId = golem1.getId();
            DealDamageToAnyTargetEffect damageEffect = new DealDamageToAnyTargetEffect(2);
            Card shockCard = createSpellCard("Shock", List.of(damageEffect));
            StackEntry spellSnapshot = spellEntry(shockCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(damageEffect), originalTargetId);

            CopySpellForEachOtherSubtypePermanentEffect effect =
                    new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM, spellSnapshot, player1Id, originalTargetId);

            Card triggerCard = createCard("Precursor Golem");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Precursor Golem trigger", List.of(effect));

            service.resolveCopyForEachOtherSubtype(gd, triggerEntry, effect);

            // No copies since the only other permanent is a Bear, not a Golem
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Does nothing when spell snapshot is null")
        void doesNothingWhenSpellSnapshotIsNull() {
            CopySpellForEachOtherSubtypePermanentEffect effect =
                    new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM, null, player1Id, UUID.randomUUID());

            Card triggerCard = createCard("Precursor Golem");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Precursor Golem trigger", List.of(effect));

            service.resolveCopyForEachOtherSubtype(gd, triggerEntry, effect);

            assertThat(gd.stack).isEmpty();
            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Copies controller matches the casting player of the original spell")
        void copiesControllerMatchesCastingPlayer() {
            Permanent golem1 = createCreaturePermanent("Precursor Golem", List.of(CardSubtype.GOLEM));
            Permanent golem2 = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            gd.playerBattlefields.get(player1Id).addAll(List.of(golem1, golem2));

            UUID originalTargetId = golem1.getId();
            DealDamageToAnyTargetEffect damageEffect = new DealDamageToAnyTargetEffect(2);
            Card shockCard = createSpellCard("Shock", List.of(damageEffect));
            StackEntry spellSnapshot = spellEntry(shockCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(damageEffect), originalTargetId);

            CopySpellForEachOtherSubtypePermanentEffect effect =
                    new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM, spellSnapshot, player1Id, originalTargetId);

            Card triggerCard = createCard("Precursor Golem");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Precursor Golem trigger", List.of(effect));

            when(validTargetService.canPermanentBeTargetedBySpell(eq(gd), any(), eq(shockCard), eq(player1Id)))
                    .thenReturn(true);

            service.resolveCopyForEachOtherSubtype(gd, triggerEntry, effect);

            gd.stack.forEach(se ->
                    assertThat(se.getControllerId()).isEqualTo(player1Id));
        }

        @Test
        @DisplayName("Opponent's Golems are also considered for copies")
        void opponentGolemsAlsoConsidered() {
            Permanent golem1 = createCreaturePermanent("Precursor Golem", List.of(CardSubtype.GOLEM));
            Permanent opponentGolem = createCreaturePermanent("Golem", List.of(CardSubtype.GOLEM));
            gd.playerBattlefields.get(player1Id).add(golem1);
            gd.playerBattlefields.get(player2Id).add(opponentGolem);

            UUID originalTargetId = golem1.getId();
            DealDamageToAnyTargetEffect damageEffect = new DealDamageToAnyTargetEffect(2);
            Card shockCard = createSpellCard("Shock", List.of(damageEffect));
            StackEntry spellSnapshot = spellEntry(shockCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(damageEffect), originalTargetId);

            CopySpellForEachOtherSubtypePermanentEffect effect =
                    new CopySpellForEachOtherSubtypePermanentEffect(CardSubtype.GOLEM, spellSnapshot, player1Id, originalTargetId);

            Card triggerCard = createCard("Precursor Golem");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Precursor Golem trigger", List.of(effect));

            when(validTargetService.canPermanentBeTargetedBySpell(gd, opponentGolem, shockCard, player1Id))
                    .thenReturn(true);

            service.resolveCopyForEachOtherSubtype(gd, triggerEntry, effect);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(opponentGolem.getId());
        }
    }

    // =========================================================================
    // resolveCopyForEachOtherPlayer — CopySpellForEachOtherPlayerEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveCopyForEachOtherPlayer")
    class ResolveCopyForEachOtherPlayer {

        @Test
        @DisplayName("Creates a copy for each other player")
        void createsCopyForEachOtherPlayer() {
            Card spellCard = createSpellCard("Syphon Mind", List.of());
            StackEntry spellSnapshot = spellEntry(spellCard, player1Id, StackEntryType.SORCERY_SPELL,
                    List.of(), null);

            CopySpellForEachOtherPlayerEffect effect =
                    new CopySpellForEachOtherPlayerEffect(spellSnapshot, player1Id);

            Card triggerCard = createCard("Radiate");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Radiate trigger", List.of(effect));

            service.resolveCopyForEachOtherPlayer(gd, triggerEntry, effect);

            // One copy for player2 (the only other player)
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().isCopy()).isTrue();
            assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("Copy preserves target from the spell snapshot")
        void copyPreservesTarget() {
            Card spellCard = createSpellCard("Lightning Bolt", List.of());
            UUID targetPermId = UUID.randomUUID();
            StackEntry spellSnapshot = spellEntry(spellCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(new DealDamageToAnyTargetEffect(3)), targetPermId);

            CopySpellForEachOtherPlayerEffect effect =
                    new CopySpellForEachOtherPlayerEffect(spellSnapshot, player1Id);

            Card triggerCard = createCard("Radiate");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Radiate trigger", List.of(effect));

            service.resolveCopyForEachOtherPlayer(gd, triggerEntry, effect);

            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetPermId);
        }

        @Test
        @DisplayName("Does nothing when spell snapshot is null")
        void doesNothingWhenSpellSnapshotIsNull() {
            CopySpellForEachOtherPlayerEffect effect =
                    new CopySpellForEachOtherPlayerEffect(null, player1Id);

            Card triggerCard = createCard("Radiate");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Radiate trigger", List.of(effect));

            service.resolveCopyForEachOtherPlayer(gd, triggerEntry, effect);

            assertThat(gd.stack).isEmpty();
            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Game log records copy creation for each other player")
        void gameLogRecordsCopyForEachPlayer() {
            Card spellCard = createSpellCard("Syphon Mind", List.of());
            StackEntry spellSnapshot = spellEntry(spellCard, player1Id, StackEntryType.SORCERY_SPELL,
                    List.of(), null);

            CopySpellForEachOtherPlayerEffect effect =
                    new CopySpellForEachOtherPlayerEffect(spellSnapshot, player1Id);

            Card triggerCard = createCard("Radiate");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Radiate trigger", List.of(effect));

            service.resolveCopyForEachOtherPlayer(gd, triggerEntry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("A copy of Syphon Mind is created for Player2."));
        }

        @Test
        @DisplayName("Queues retarget PendingMayAbility when copy has a target")
        void queuesRetargetMayAbilityWhenCopyHasTarget() {
            Card spellCard = createSpellCard("Lightning Bolt", List.of());
            UUID targetPermId = UUID.randomUUID();
            StackEntry spellSnapshot = spellEntry(spellCard, player1Id, StackEntryType.INSTANT_SPELL,
                    List.of(new DealDamageToAnyTargetEffect(3)), targetPermId);

            CopySpellForEachOtherPlayerEffect effect =
                    new CopySpellForEachOtherPlayerEffect(spellSnapshot, player1Id);

            Card triggerCard = createCard("Radiate");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Radiate trigger", List.of(effect));

            service.resolveCopyForEachOtherPlayer(gd, triggerEntry, effect);

            assertThat(gd.pendingMayAbilities).hasSize(1);
            PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
            assertThat(ability.controllerId()).isEqualTo(player2Id);
            assertThat(ability.description()).contains("Choose new targets");
        }

        @Test
        @DisplayName("No retarget PendingMayAbility when copy has no target")
        void noRetargetMayAbilityWhenNoTarget() {
            Card spellCard = createSpellCard("Syphon Mind", List.of());
            StackEntry spellSnapshot = spellEntry(spellCard, player1Id, StackEntryType.SORCERY_SPELL,
                    List.of(), null);

            CopySpellForEachOtherPlayerEffect effect =
                    new CopySpellForEachOtherPlayerEffect(spellSnapshot, player1Id);

            Card triggerCard = createCard("Radiate");
            StackEntry triggerEntry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, triggerCard, player1Id,
                    "Radiate trigger", List.of(effect));

            service.resolveCopyForEachOtherPlayer(gd, triggerEntry, effect);

            assertThat(gd.pendingMayAbilities).isEmpty();
        }
    }

    // =========================================================================
    // resolveBecomeCopyOfTargetCreature — BecomeCopyOfTargetCreatureEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveBecomeCopyOfTargetCreature")
    class ResolveBecomeCopyOfTargetCreature {

        @Test
        @DisplayName("Queues a PendingMayAbility for the become-copy choice")
        void queuesMayAbilityForBecomeCopyChoice() {
            Card cloneCard = createCard("Clone");
            Permanent targetCreature = createCreaturePermanent("Serra Angel", List.of(CardSubtype.ANGEL));
            gd.playerBattlefields.get(player2Id).add(targetCreature);

            StackEntry entry = new StackEntry(StackEntryType.CREATURE_SPELL, cloneCard, player1Id,
                    cloneCard.getName(), List.of(new BecomeCopyOfTargetCreatureEffect()), 0,
                    targetCreature.getId(), null, null, null, null, null);

            when(gameQueryService.findPermanentById(gd, targetCreature.getId())).thenReturn(targetCreature);

            service.resolveBecomeCopyOfTargetCreature(gd, entry);

            assertThat(gd.pendingMayAbilities).hasSize(1);
            PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
            assertThat(ability.controllerId()).isEqualTo(player1Id);
            assertThat(ability.description()).contains("Clone");
            assertThat(ability.description()).contains("Serra Angel");
            assertThat(ability.targetCardId()).isEqualTo(targetCreature.getId());
        }

        @Test
        @DisplayName("Does nothing when targetId is null")
        void doesNothingWhenTargetIdIsNull() {
            Card cloneCard = createCard("Clone");
            StackEntry entry = new StackEntry(StackEntryType.CREATURE_SPELL, cloneCard, player1Id,
                    cloneCard.getName(), List.of(new BecomeCopyOfTargetCreatureEffect()), 0,
                    (UUID) null, null, null, null, null, null);

            service.resolveBecomeCopyOfTargetCreature(gd, entry);

            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Does nothing when target creature is no longer on the battlefield")
        void doesNothingWhenTargetCreatureGone() {
            Card cloneCard = createCard("Clone");
            UUID removedCreatureId = UUID.randomUUID();
            StackEntry entry = new StackEntry(StackEntryType.CREATURE_SPELL, cloneCard, player1Id,
                    cloneCard.getName(), List.of(new BecomeCopyOfTargetCreatureEffect()), 0,
                    removedCreatureId, null, null, null, null, null);

            when(gameQueryService.findPermanentById(gd, removedCreatureId)).thenReturn(null);

            service.resolveBecomeCopyOfTargetCreature(gd, entry);

            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("PendingMayAbility effects contain BecomeCopyOfTargetCreatureEffect")
        void mayAbilityEffectsContainBecomeCopyEffect() {
            Card cloneCard = createCard("Clone");
            Permanent targetCreature = createCreaturePermanent("Grizzly Bears", List.of(CardSubtype.BEAR));
            gd.playerBattlefields.get(player2Id).add(targetCreature);

            StackEntry entry = new StackEntry(StackEntryType.CREATURE_SPELL, cloneCard, player1Id,
                    cloneCard.getName(), List.of(new BecomeCopyOfTargetCreatureEffect()), 0,
                    targetCreature.getId(), null, null, null, null, null);

            when(gameQueryService.findPermanentById(gd, targetCreature.getId())).thenReturn(targetCreature);

            service.resolveBecomeCopyOfTargetCreature(gd, entry);

            PendingMayAbility ability = gd.pendingMayAbilities.getFirst();
            assertThat(ability.effects())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(BecomeCopyOfTargetCreatureEffect.class);
        }
    }
}
