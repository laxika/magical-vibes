package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CopySpellForEachOtherSubtypePermanentEffectHandlerTest {

    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private ValidTargetService validTargetService;
    @Mock private GameQueryService gameQueryService;
    @Mock private CloneService cloneService;
    private final CopySupport copySupport = new CopySupport();
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private CopySpellForEachOtherSubtypePermanentEffectHandler copySpellForEachOtherSubtypeHandler;

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
        copySpellForEachOtherSubtypeHandler = new CopySpellForEachOtherSubtypePermanentEffectHandler(
                gameBroadcastService, validTargetService, copySupport);

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
            @DisplayName("Creates copies for each other Golem â€” 2 copies for 3 Golems")
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

                copySpellForEachOtherSubtypeHandler.resolve(gd, triggerEntry, effect);

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

                copySpellForEachOtherSubtypeHandler.resolve(gd, triggerEntry, effect);

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

                copySpellForEachOtherSubtypeHandler.resolve(gd, triggerEntry, effect);

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

                copySpellForEachOtherSubtypeHandler.resolve(gd, triggerEntry, effect);

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

                copySpellForEachOtherSubtypeHandler.resolve(gd, triggerEntry, effect);

                verify(gameBroadcastService, times(2)).logAndBroadcast(eq(gd),
                        eq("A copy of Shock is created targeting Golem."));
            }

            @Test
            @DisplayName("Untargetable Golem is skipped â€” no copy targets it")
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

                copySpellForEachOtherSubtypeHandler.resolve(gd, triggerEntry, effect);

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

                copySpellForEachOtherSubtypeHandler.resolve(gd, triggerEntry, effect);

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

                copySpellForEachOtherSubtypeHandler.resolve(gd, triggerEntry, effect);

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

                copySpellForEachOtherSubtypeHandler.resolve(gd, triggerEntry, effect);

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

                copySpellForEachOtherSubtypeHandler.resolve(gd, triggerEntry, effect);

                assertThat(gd.stack).hasSize(1);
                assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(opponentGolem.getId());
            }
}
