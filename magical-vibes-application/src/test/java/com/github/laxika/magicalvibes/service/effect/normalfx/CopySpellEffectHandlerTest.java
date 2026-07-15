package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.BeforeEach;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.Test;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.extension.ExtendWith;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.mockito.Mock;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.mockito.junit.jupiter.MockitoExtension;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.ArrayList;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.Collections;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.mockito.ArgumentMatchers.any;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.mockito.ArgumentMatchers.anyString;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.mockito.Mockito.never;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import static org.mockito.Mockito.verify;
import com.github.laxika.magicalvibes.model.amount.Fixed;

@ExtendWith(MockitoExtension.class)
class CopySpellEffectHandlerTest {

    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private ValidTargetService validTargetService;
    @Mock private GameQueryService gameQueryService;
    @Mock private CloneService cloneService;
    private final CopySupport copySupport = new CopySupport();
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private CopySpellEffectHandler copySpellHandler;

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
        copySpellHandler = new CopySpellEffectHandler(gameBroadcastService, copySupport);

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
        // resolveCopySpell â€” CopySpellEffect
        // =========================================================================

    @Test
            @DisplayName("Copy preserves entry type from target spell")
            void copyPreservesEntryType() {
                Card counselCard = createSpellCard("Counsel of the Soratami", List.of());
                StackEntry targetEntry = spellEntry(counselCard, player1Id, StackEntryType.SORCERY_SPELL,
                        List.of(), null);
                gd.stack.add(targetEntry);

                Card twincastCard = createCard("Twincast");
                StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, counselCard.getId());

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

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

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

                StackEntry copyEntry = gd.stack.getLast();
                assertThat(copyEntry.getEffectsToResolve())
                        .hasSize(1)
                        .first()
                        .isInstanceOf(DealDamageToAnyTargetEffect.class);

                DealDamageToAnyTargetEffect copiedEffect = (DealDamageToAnyTargetEffect) copyEntry.getEffectsToResolve().getFirst();
                assertThat(copiedEffect.damage()).isEqualTo(new Fixed(2));
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

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

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

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

                StackEntry copyEntry = gd.stack.getLast();
                assertThat(copyEntry.getControllerId()).isEqualTo(player2Id);
                assertThat(copyEntry.getControllerId()).isNotEqualTo(player1Id);
            }

            @Test
            @DisplayName("Copy card has a new identity â€” different UUID from original")
            void copyHasNewCardIdentity() {
                Card counselCard = createSpellCard("Counsel of the Soratami", List.of());
                StackEntry targetEntry = spellEntry(counselCard, player1Id, StackEntryType.SORCERY_SPELL,
                        List.of(), null);
                gd.stack.add(targetEntry);

                Card twincastCard = createCard("Twincast");
                StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, counselCard.getId());

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

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

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

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
                        List.of(ReturnToHandEffect.target()), bearsPermId);
                gd.stack.add(targetEntry);

                Card twincastCard = createCard("Twincast");
                StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, boomerangCard.getId());

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

                StackEntry copyEntry = gd.stack.getLast();
                assertThat(copyEntry.getDescription()).isEqualTo("Copy of Boomerang");
            }

            @Test
            @DisplayName("Copy preserves target from original spell")
            void copyPreservesTarget() {
                Card boomerangCard = createSpellCard("Boomerang", List.of());
                UUID bearsPermId = UUID.randomUUID();
                StackEntry targetEntry = spellEntry(boomerangCard, player1Id, StackEntryType.INSTANT_SPELL,
                        List.of(ReturnToHandEffect.target()), bearsPermId);
                gd.stack.add(targetEntry);

                Card twincastCard = createCard("Twincast");
                StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, boomerangCard.getId());

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

                StackEntry copyEntry = gd.stack.getLast();
                assertThat(copyEntry.getTargetId()).isEqualTo(bearsPermId);
            }

            @Test
            @DisplayName("No copy created when target spell was removed from stack")
            void noCopyWhenTargetSpellRemoved() {
                // Stack is empty â€” target spell already removed
                Card twincastCard = createCard("Twincast");
                UUID removedCardId = UUID.randomUUID();
                StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, removedCardId);

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

                assertThat(gd.stack).isEmpty();
                verify(gameBroadcastService, never()).logAndBroadcast(any(), any(GameLogEntry.class));
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

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

                verify(gameBroadcastService).logAndBroadcast(eq(gd), eq(GameLog.text("A copy of Counsel of the Soratami is created.")));
            }

            @Test
            @DisplayName("Does nothing when targetId is null")
            void doesNothingWhenTargetIdIsNull() {
                Card twincastCard = createCard("Twincast");
                StackEntry twincastEntry = new StackEntry(StackEntryType.INSTANT_SPELL, twincastCard, player2Id,
                        twincastCard.getName(), List.of(new CopySpellEffect()), 0,
                        (UUID) null, null, null, null, null, null);

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

                assertThat(gd.stack).isEmpty();
                verify(gameBroadcastService, never()).logAndBroadcast(any(), any(GameLogEntry.class));
            }

            @Test
            @DisplayName("Queues a retarget PendingMayAbility when the copy has a target")
            void queuesRetargetMayAbilityWhenCopyHasTarget() {
                Card boomerangCard = createSpellCard("Boomerang", List.of());
                UUID bearsPermId = UUID.randomUUID();
                StackEntry targetEntry = spellEntry(boomerangCard, player1Id, StackEntryType.INSTANT_SPELL,
                        List.of(ReturnToHandEffect.target()), bearsPermId);
                gd.stack.add(targetEntry);

                Card twincastCard = createCard("Twincast");
                StackEntry twincastEntry = copySpellTriggerEntry(twincastCard, player2Id, boomerangCard.getId());

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

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

                copySpellHandler.resolve(gd, twincastEntry, new CopySpellEffect());

                assertThat(gd.pendingMayAbilities).isEmpty();
            }
}
