package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CopySpellForEachOtherPlayerEffectHandlerTest {

    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private ValidTargetService validTargetService;
    @Mock private GameQueryService gameQueryService;
    @Mock private CloneService cloneService;
    private final CopySupport copySupport = new CopySupport();
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private CopySpellForEachOtherPlayerEffectHandler copySpellForEachOtherPlayerHandler;

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
        copySpellForEachOtherPlayerHandler = new CopySpellForEachOtherPlayerEffectHandler(
                gameBroadcastService, copySupport);

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

                copySpellForEachOtherPlayerHandler.resolve(gd, triggerEntry, effect);

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

                copySpellForEachOtherPlayerHandler.resolve(gd, triggerEntry, effect);

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

                copySpellForEachOtherPlayerHandler.resolve(gd, triggerEntry, effect);

                assertThat(gd.stack).isEmpty();
                verify(gameBroadcastService, never()).logAndBroadcast(any(), any(GameLogEntry.class));
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

                copySpellForEachOtherPlayerHandler.resolve(gd, triggerEntry, effect);

                verify(gameBroadcastService).logAndBroadcast(eq(gd), eq(GameLog.text("A copy of Syphon Mind is created for Player2.")));
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

                copySpellForEachOtherPlayerHandler.resolve(gd, triggerEntry, effect);

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

                copySpellForEachOtherPlayerHandler.resolve(gd, triggerEntry, effect);

                assertThat(gd.pendingMayAbilities).isEmpty();
            }
}
