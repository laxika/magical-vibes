package com.github.laxika.magicalvibes.service.state;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DelayedPlusOnePlusOneCounterRegrowthEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StateBasedActionServiceTest {

    @Mock
    private GameOutcomeService gameOutcomeService;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    @Mock
    private GraveyardService graveyardService;
    @Mock
    private StateTriggerService stateTriggerService;

    @InjectMocks
    private StateBasedActionService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.playerBattlefields.put(player1Id, new ArrayList<>());
        gd.playerBattlefields.put(player2Id, new ArrayList<>());
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerManaPools.put(player2Id, new ManaPool());
    }

    private static Card createCreatureCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        return card;
    }

    private static Card createPlaneswalkerCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.PLANESWALKER);
        return card;
    }

    private static Card createSagaCard(String name, int finalChapter) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SAGA));
        // Add chapter effects up to finalChapter so getSagaFinalChapter() returns the right value
        if (finalChapter >= 1) card.addEffect(EffectSlot.SAGA_CHAPTER_I, new DealDamageToAnyTargetEffect(1));
        if (finalChapter >= 2) card.addEffect(EffectSlot.SAGA_CHAPTER_II, new DealDamageToAnyTargetEffect(1));
        if (finalChapter >= 3) card.addEffect(EffectSlot.SAGA_CHAPTER_III, new DealDamageToAnyTargetEffect(1));
        return card;
    }

    @Nested
    @DisplayName("Creature zero toughness — CR 704.5f")
    class CreatureZeroToughness {

        @Test
        @DisplayName("Creature with 0 effective toughness is put into the graveyard")
        void creatureWithZeroToughnessDies() {
            Card card = createCreatureCard("Scornful Egotist");
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(0);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Scornful Egotist is put into the graveyard (0 toughness)."));
        }

        @Test
        @DisplayName("Creature with negative effective toughness is put into the graveyard")
        void creatureWithNegativeToughnessDies() {
            Card card = createCreatureCard("Weakened Creature");
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(-2);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
        }
    }

    @Nested
    @DisplayName("Lethal damage — CR 704.5g")
    class LethalDamage {

        @Test
        @DisplayName("Creature with damage >= toughness is destroyed")
        void creatureWithLethalDamageIsDestroyed() {
            Card card = createCreatureCard("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(3);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(2);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, perm)).thenReturn(false);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is destroyed (lethal damage)."));
        }

        @Test
        @DisplayName("Creature with damage equal to toughness is destroyed")
        void creatureWithDamageEqualToToughnessIsDestroyed() {
            Card card = createCreatureCard("Hill Giant");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(3);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(3);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, perm)).thenReturn(false);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Indestructible creature with lethal damage survives")
        void indestructibleCreatureSurvivesLethalDamage() {
            Card card = createCreatureCard("Darksteel Colossus");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(10);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(2);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.INDESTRUCTIBLE)).thenReturn(true);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Regenerating creature survives lethal damage")
        void regeneratingCreatureSurvivesLethalDamage() {
            Card card = createCreatureCard("Troll Ascetic");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(5);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(2);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, perm)).thenReturn(true);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Creature with damage less than toughness survives")
        void creatureWithLessDamageSurvives() {
            Card card = createCreatureCard("Grizzly Bears");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(1);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(2);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Creature with 0 toughness AND lethal damage logs as 0 toughness (first branch wins)")
        void zeroToughnessTakesPrecedenceOverLethalDamage() {
            Card card = createCreatureCard("Enfeebled Creature");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(5);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(0);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Enfeebled Creature is put into the graveyard (0 toughness)."));
        }
    }

    @Nested
    @DisplayName("Planeswalker zero loyalty — CR 704.5i")
    class PlaneswalkerZeroLoyalty {

        @Test
        @DisplayName("Planeswalker with 0 loyalty counters dies")
        void planeswalkerWithZeroLoyaltyDies() {
            Card card = createPlaneswalkerCard("Jace Beleren");
            Permanent perm = new Permanent(card);
            perm.setLoyaltyCounters(0);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Jace Beleren has no loyalty counters and is put into the graveyard."));
        }

        @Test
        @DisplayName("Planeswalker with negative loyalty counters dies")
        void planeswalkerWithNegativeLoyaltyDies() {
            Card card = createPlaneswalkerCard("Chandra Nalaar");
            Permanent perm = new Permanent(card);
            perm.setLoyaltyCounters(-1);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Creature-planeswalker with healthy toughness but 0 loyalty dies via loyalty branch")
        void creaturePlaneswalkerDiesToZeroLoyalty() {
            Card card = new Card();
            card.setName("Gideon Jura");
            card.setType(CardType.PLANESWALKER);
            Permanent perm = new Permanent(card);
            perm.setLoyaltyCounters(0);
            gd.playerBattlefields.get(player1Id).add(perm);

            // isCreature true (animated planeswalker), but toughness is healthy
            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(6);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
            // Death reason is captured at collection time: creature checks fail (healthy toughness,
            // no lethal damage), but planeswalker check succeeds (0 loyalty)
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Gideon Jura has no loyalty counters and is put into the graveyard."));
        }

        @Test
        @DisplayName("Planeswalker with positive loyalty counters survives")
        void planeswalkerWithPositiveLoyaltySurvives() {
            Card card = createPlaneswalkerCard("Liliana Vess");
            Permanent perm = new Permanent(card);
            perm.setLoyaltyCounters(3);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
        }
    }

    @Nested
    @DisplayName("Orphaned auras cleanup")
    class OrphanedAuras {

        @Test
        @DisplayName("Orphaned auras are removed when any creature died")
        void orphanedAurasRemovedWhenCreatureDied() {
            Card card = createCreatureCard("Grizzly Bears");
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(0);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Orphaned auras are removed when a planeswalker died")
        void orphanedAurasRemovedWhenPlaneswalkerDied() {
            Card card = createPlaneswalkerCard("Jace Beleren");
            Permanent perm = new Permanent(card);
            perm.setLoyaltyCounters(0);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Orphaned auras are not removed when nothing died")
        void orphanedAurasNotRemovedWhenNothingDied() {
            // empty battlefield
            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removeOrphanedAuras(gd);
        }
    }

    @Nested
    @DisplayName("Win condition check — CR 704.5a/c")
    class WinConditionCheck {

        @Test
        @DisplayName("Checks win condition after creature death processing")
        void checksWinCondition() {
            sut.performStateBasedActions(gd);

            verify(gameOutcomeService).checkWinCondition(gd);
        }

        @Test
        @DisplayName("Short-circuits when win condition is met")
        void shortCircuitsOnWinCondition() {
            when(gameOutcomeService.checkWinCondition(gd)).thenReturn(true);

            sut.performStateBasedActions(gd);

            // Saga checks and counter annihilation should NOT run
            verify(stateTriggerService, never()).checkStateTriggers(gd);
        }
    }

    @Nested
    @DisplayName("Saga sacrifice — CR 714.4")
    class SagaSacrifice {

        @Test
        @DisplayName("Saga with lore counters >= final chapter is sacrificed")
        void sagaWithFinalChapterReachedIsSacrificed() {
            Card card = createSagaCard("The Eldest Reborn", 3);
            Permanent perm = new Permanent(card);
            perm.setLoreCounters(3);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("The Eldest Reborn is sacrificed (final chapter reached)."));
        }

        @Test
        @DisplayName("Saga with more lore counters than final chapter is sacrificed")
        void sagaWithExcessLoreCountersIsSacrificed() {
            Card card = createSagaCard("History of Benalia", 3);
            Permanent perm = new Permanent(card);
            perm.setLoreCounters(5);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Saga with lore counters below final chapter is not sacrificed")
        void sagaBelowFinalChapterSurvives() {
            Card card = createSagaCard("Phyrexian Scriptures", 3);
            Permanent perm = new Permanent(card);
            perm.setLoreCounters(2);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Saga is not sacrificed if chapter ability from it is still on the stack")
        void sagaNotSacrificedWhenChapterAbilityOnStack() {
            Card card = createSagaCard("The Flame of Keld", 3);
            Permanent perm = new Permanent(card);
            perm.setLoreCounters(3);
            gd.playerBattlefields.get(player1Id).add(perm);

            // Put a triggered ability from this Saga on the stack
            StackEntry chapterAbility = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                    "Chapter III", List.of(), null, perm.getId());
            gd.stack.add(chapterAbility);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Saga is sacrificed when stack has unrelated triggered ability")
        void sagaSacrificedWhenStackHasUnrelatedAbility() {
            Card card = createSagaCard("The Mirari Conjecture", 3);
            Permanent perm = new Permanent(card);
            perm.setLoreCounters(3);
            gd.playerBattlefields.get(player1Id).add(perm);

            // Unrelated triggered ability on the stack (different source permanent)
            Card otherCard = createCreatureCard("Some Creature");
            StackEntry unrelatedAbility = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, otherCard, player1Id,
                    "ETB trigger", List.of(), null, UUID.randomUUID());
            gd.stack.add(unrelatedAbility);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Non-TRIGGERED_ABILITY stack entry from same source does not block sacrifice")
        void nonTriggeredAbilityFromSameSourceDoesNotBlock() {
            Card card = createSagaCard("The Antiquities War", 3);
            Permanent perm = new Permanent(card);
            perm.setLoreCounters(3);
            gd.playerBattlefields.get(player1Id).add(perm);

            // A sorcery spell entry with the same sourcePermanentId but different entry type
            StackEntry spellEntry = new StackEntry(
                    StackEntryType.SORCERY_SPELL, card, player1Id,
                    "Sorcery", List.of(), null, perm.getId());
            gd.stack.add(spellEntry);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Saga with no chapter effects (finalChapter 0) is not sacrificed")
        void sagaWithNoChapterEffectsNotSacrificed() {
            Card card = new Card();
            card.setName("Empty Saga");
            card.setType(CardType.ENCHANTMENT);
            card.setSubtypes(List.of(CardSubtype.SAGA));
            // No chapter effects → getSagaFinalChapter() returns 0
            Permanent perm = new Permanent(card);
            perm.setLoreCounters(5);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Non-saga enchantment is not sacrificed regardless of lore counters")
        void nonSagaEnchantmentNotSacrificed() {
            Card card = new Card();
            card.setName("Propaganda");
            card.setType(CardType.ENCHANTMENT);
            Permanent perm = new Permanent(card);
            perm.setLoreCounters(10);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
        }
    }

    @Nested
    @DisplayName("Counter annihilation — CR 704.5q")
    class CounterAnnihilation {

        @Test
        @DisplayName("+1/+1 and -1/-1 counters cancel each other out")
        void countersCancelOut() {
            Card card = createCreatureCard("Cytoplast Root-Kin");
            Permanent perm = new Permanent(card);
            perm.setPlusOnePlusOneCounters(5);
            perm.setMinusOneMinusOneCounters(3);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(perm.getPlusOnePlusOneCounters()).isEqualTo(2);
            assertThat(perm.getMinusOneMinusOneCounters()).isZero();
        }

        @Test
        @DisplayName("Both counter types become zero when equal")
        void bothBecomeZeroWhenEqual() {
            Card card = createCreatureCard("Fertilid");
            Permanent perm = new Permanent(card);
            perm.setPlusOnePlusOneCounters(4);
            perm.setMinusOneMinusOneCounters(4);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(perm.getPlusOnePlusOneCounters()).isZero();
            assertThat(perm.getMinusOneMinusOneCounters()).isZero();
        }

        @Test
        @DisplayName("More -1/-1 than +1/+1 leaves only -1/-1 counters")
        void moreMinusThanPlusLeavesMinusCounters() {
            Card card = createCreatureCard("Devoted Druid");
            Permanent perm = new Permanent(card);
            perm.setPlusOnePlusOneCounters(2);
            perm.setMinusOneMinusOneCounters(5);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(perm.getPlusOnePlusOneCounters()).isZero();
            assertThat(perm.getMinusOneMinusOneCounters()).isEqualTo(3);
        }

        @Test
        @DisplayName("No cancellation when only +1/+1 counters present")
        void noChangeWhenOnlyPlusCounters() {
            Card card = createCreatureCard("Slith Firewalker");
            Permanent perm = new Permanent(card);
            perm.setPlusOnePlusOneCounters(3);
            perm.setMinusOneMinusOneCounters(0);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(perm.getPlusOnePlusOneCounters()).isEqualTo(3);
            assertThat(perm.getMinusOneMinusOneCounters()).isZero();
        }

        @Test
        @DisplayName("No cancellation when only -1/-1 counters present")
        void noChangeWhenOnlyMinusCounters() {
            Card card = createCreatureCard("Fading Creature");
            Permanent perm = new Permanent(card);
            perm.setPlusOnePlusOneCounters(0);
            perm.setMinusOneMinusOneCounters(2);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(perm.getPlusOnePlusOneCounters()).isZero();
            assertThat(perm.getMinusOneMinusOneCounters()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Delayed +1/+1 counter regrowth (Protean Hydra ruling)")
    class DelayedCounterRegrowth {

        @Test
        @DisplayName("Counter annihilation triggers delayed regrowth for permanents with the effect")
        void counterAnnihilationTriggersRegrowth() {
            Card card = createCreatureCard("Protean Hydra");
            card.addEffect(EffectSlot.STATIC, new DelayedPlusOnePlusOneCounterRegrowthEffect());
            Permanent perm = new Permanent(card);
            perm.setPlusOnePlusOneCounters(5);
            perm.setMinusOneMinusOneCounters(3);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            // 3 counters cancelled => 3*2 = 6 pending regrowth counters
            assertThat(gd.pendingDelayedPlusOnePlusOneCounters.get(perm.getId())).isEqualTo(6);
        }

        @Test
        @DisplayName("No delayed regrowth for permanents without the effect")
        void noRegrowthWithoutEffect() {
            Card card = createCreatureCard("Regular Creature");
            Permanent perm = new Permanent(card);
            perm.setPlusOnePlusOneCounters(3);
            perm.setMinusOneMinusOneCounters(2);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(gd.pendingDelayedPlusOnePlusOneCounters).doesNotContainKey(perm.getId());
        }

        @Test
        @DisplayName("Regrowth stacks with existing pending counters")
        void regrowthStacksWithExisting() {
            Card card = createCreatureCard("Protean Hydra");
            card.addEffect(EffectSlot.STATIC, new DelayedPlusOnePlusOneCounterRegrowthEffect());
            Permanent perm = new Permanent(card);
            perm.setPlusOnePlusOneCounters(4);
            perm.setMinusOneMinusOneCounters(2);
            gd.playerBattlefields.get(player1Id).add(perm);
            gd.pendingDelayedPlusOnePlusOneCounters.put(perm.getId(), 4);

            sut.performStateBasedActions(gd);

            // existing 4 + (2 cancelled * 2) = 8
            assertThat(gd.pendingDelayedPlusOnePlusOneCounters.get(perm.getId())).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("State triggers — CR 603.8")
    class StateTriggers {

        @Test
        @DisplayName("State triggers are checked after SBAs")
        void stateTriggersCheckedAfterSBAs() {
            sut.performStateBasedActions(gd);

            verify(stateTriggerService).checkStateTriggers(gd);
        }
    }

    @Nested
    @DisplayName("Draw from empty library — CR 704.5b")
    class DrawFromEmptyLibrary {

        @Test
        @DisplayName("Player who drew from empty library loses the game")
        void playerLosesWhenDrawingFromEmptyLibrary() {
            gd.playersAttemptedDrawFromEmptyLibrary.add(player1Id);
            when(gameQueryService.canPlayerLoseGame(gd, player1Id)).thenReturn(true);
            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            sut.performStateBasedActions(gd);

            verify(gameOutcomeService).declareWinner(gd, player2Id);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Player1 attempted to draw from an empty library and loses the game."));
        }

        @Test
        @DisplayName("playersAttemptedDrawFromEmptyLibrary is cleared after processing")
        void setIsClearedAfterProcessing() {
            gd.playersAttemptedDrawFromEmptyLibrary.add(player1Id);
            when(gameQueryService.canPlayerLoseGame(gd, player1Id)).thenReturn(true);
            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            sut.performStateBasedActions(gd);

            assertThat(gd.playersAttemptedDrawFromEmptyLibrary).isEmpty();
        }

        @Test
        @DisplayName("Player with CantLoseGameEffect does not lose from empty library draw")
        void cantLosePlayerDoesNotLose() {
            gd.playersAttemptedDrawFromEmptyLibrary.add(player1Id);
            when(gameQueryService.canPlayerLoseGame(gd, player1Id)).thenReturn(false);

            sut.performStateBasedActions(gd);

            verify(gameOutcomeService, never()).declareWinner(any(), any());
        }

        @Test
        @DisplayName("Set is cleared even when player cannot lose")
        void setIsClearedEvenWhenPlayerCannotLose() {
            gd.playersAttemptedDrawFromEmptyLibrary.add(player1Id);
            when(gameQueryService.canPlayerLoseGame(gd, player1Id)).thenReturn(false);

            sut.performStateBasedActions(gd);

            assertThat(gd.playersAttemptedDrawFromEmptyLibrary).isEmpty();
        }

        @Test
        @DisplayName("No loss check when no player attempted draw from empty library")
        void noCheckWhenSetIsEmpty() {
            sut.performStateBasedActions(gd);

            verify(gameOutcomeService, never()).declareWinner(any(), any());
        }

        @Test
        @DisplayName("Both players drawing from empty library results in both loss checks")
        void bothPlayersDrawFromEmptyLibrary() {
            gd.playersAttemptedDrawFromEmptyLibrary.add(player1Id);
            gd.playersAttemptedDrawFromEmptyLibrary.add(player2Id);
            when(gameQueryService.canPlayerLoseGame(gd, player1Id)).thenReturn(true);
            when(gameQueryService.canPlayerLoseGame(gd, player2Id)).thenReturn(true);
            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);
            when(gameQueryService.getOpponentId(gd, player2Id)).thenReturn(player1Id);

            sut.performStateBasedActions(gd);

            verify(gameOutcomeService).declareWinner(gd, player2Id);
            verify(gameOutcomeService).declareWinner(gd, player1Id);
            assertThat(gd.playersAttemptedDrawFromEmptyLibrary).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple players and permanents")
    class MultiplePlayersAndPermanents {

        @Test
        @DisplayName("Processes creatures from both players' battlefields")
        void processesCreaturesFromBothPlayers() {
            Card card1 = createCreatureCard("Player1 Creature");
            Permanent perm1 = new Permanent(card1);
            gd.playerBattlefields.get(player1Id).add(perm1);

            Card card2 = createCreatureCard("Player2 Creature");
            Permanent perm2 = new Permanent(card2);
            gd.playerBattlefields.get(player2Id).add(perm2);

            when(gameQueryService.isCreature(gd, perm1)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm1)).thenReturn(0);
            when(gameQueryService.isCreature(gd, perm2)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm2)).thenReturn(0);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm1);
            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm2);
        }

        @Test
        @DisplayName("Multiple creatures on same battlefield: only those meeting death conditions die")
        void mixOfDyingAndSurvivingOnSameBattlefield() {
            Card dyingCard = createCreatureCard("Doomed Creature");
            Permanent dyingPerm = new Permanent(dyingCard);
            Card survivingCard = createCreatureCard("Healthy Creature");
            Permanent survivingPerm = new Permanent(survivingCard);
            gd.playerBattlefields.get(player1Id).add(dyingPerm);
            gd.playerBattlefields.get(player1Id).add(survivingPerm);

            when(gameQueryService.isCreature(gd, dyingPerm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, dyingPerm)).thenReturn(0);
            when(gameQueryService.isCreature(gd, survivingPerm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, survivingPerm)).thenReturn(3);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, dyingPerm);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, survivingPerm);
        }

        @Test
        @DisplayName("Non-creature permanent with positive toughness is not affected")
        void nonCreaturePermanentNotAffected() {
            Card card = new Card();
            card.setName("Sol Ring");
            card.setType(CardType.ARTIFACT);
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
        }
    }
}
