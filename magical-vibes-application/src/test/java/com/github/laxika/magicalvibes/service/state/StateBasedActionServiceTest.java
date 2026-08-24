package com.github.laxika.magicalvibes.service.state;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;

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
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.outcome.LossOutcome;
import com.github.laxika.magicalvibes.service.outcome.LossReason;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
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
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;

@ExtendWith(MockitoExtension.class)
class StateBasedActionServiceTest {

    private static final GameQueryService.StaticBonus EMPTY_BONUS = new GameQueryService.StaticBonus(
            0, 0, java.util.Set.of(), java.util.Set.of(), false, List.of(), List.of(), java.util.Set.of(),
            List.of(), java.util.Set.of(), java.util.Set.of(), false, false, false, false, java.util.Set.of(),
            false, 0, 0, false, false);

    @Mock
    private GameOutcomeService gameOutcomeService;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private GameLogService gameLogService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    @Mock
    private GraveyardService graveyardService;
    @Mock
    private StateTriggerService stateTriggerService;
    @Mock
    private TriggerCollectionService triggerCollectionService;
    @Mock
    private LegendRuleService legendRuleService;
    @Mock
    private BattleDefeatSupport battleDefeatSupport;
    @Mock
    private CreatureControlService creatureControlService;

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
        lenient().when(gameQueryService.hasKeyword(any(GameData.class), any(Permanent.class),
                eq(Keyword.START_YOUR_ENGINES))).thenReturn(false);
        // Lethal-damage SBA reads losesAllAbilities via computeStaticBonus (Ogre Enforcer path).
        lenient().when(gameQueryService.computeStaticBonus(any(), any())).thenReturn(EMPTY_BONUS);
        lenient().when(gameQueryService.withQueryScope(any(), any())).thenAnswer(invocation ->
                invocation.<Supplier<?>>getArgument(1).get());
    }

    @Test
    @DisplayName("Lethal permanent checks share one read-only query scope")
    void lethalPermanentChecksShareQueryScope() {
        gd.playerBattlefields.get(player1Id).add(new Permanent(createCreatureCard("Creature")));

        sut.performStateBasedActions(gd);

        verify(gameQueryService, atLeastOnce()).withQueryScope(eq(gd), any());
    }

    private static Card createCreatureCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        return card;
    }

    private static Card createCreatureToken(String name) {
        Card card = createCreatureCard(name);
        card.setToken(true);
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

    @Test
    @DisplayName("Tokens cease to exist in every non-battlefield zone")
    void tokensCeaseToExistOutsideBattlefield() {
        Card libraryToken = createCreatureToken("Library Token");
        Card handToken = createCreatureToken("Hand Token");
        Card graveyardToken = createCreatureToken("Graveyard Token");
        Card commandToken = createCreatureToken("Command Token");
        Card exileToken = createCreatureToken("Exile Token");
        Card battlefieldToken = createCreatureToken("Battlefield Token");
        Card libraryCard = createCreatureCard("Library Card");

        gd.playerDecks.put(player1Id, new ArrayList<>(List.of(libraryCard, libraryToken)));
        gd.playerHands.put(player1Id, new ArrayList<>(List.of(handToken)));
        gd.playerGraveyards.put(player1Id, new ArrayList<>(List.of(graveyardToken)));
        gd.playerCommandZones.put(player1Id, new ArrayList<>(List.of(commandToken)));
        gd.addToExile(player1Id, exileToken);
        gd.playerBattlefields.get(player1Id).add(new Permanent(battlefieldToken));

        sut.performStateBasedActions(gd);

        assertThat(gd.playerDecks.get(player1Id)).containsExactly(libraryCard);
        assertThat(gd.playerHands.get(player1Id)).isEmpty();
        assertThat(gd.playerGraveyards.get(player1Id)).isEmpty();
        assertThat(gd.playerCommandZones.get(player1Id)).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1Id)).isEmpty();
        assertThat(gd.playerBattlefields.get(player1Id))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Battlefield Token");
        verify(gameLogService, times(5)).append(eq(gd), any(GameLogEntry.class));
    }

    @Nested
    @DisplayName("Deathtouch damage — CR 704.5h")
    class DeathtouchDamage {

        @Test
        @DisplayName("Creature dealt deathtouch damage is destroyed even below lethal marked damage")
        void deathtouchDamagedCreatureIsDestroyed() {
            Card card = createCreatureCard("Serra Angel");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(1);
            perm.setDamagedByDeathtouch(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(4);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, perm)).thenReturn(false);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Indestructible creature survives deathtouch damage and the memory is consumed")
        void indestructibleSurvivesDeathtouchAndFlagIsConsumed() {
            Card card = createCreatureCard("Darksteel Myr");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(1);
            perm.setDamagedByDeathtouch(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(1);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.INDESTRUCTIBLE)).thenReturn(true);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
            // CR 704.5h only spans "since the last state-based check" — the check consumes it,
            // so losing indestructible later must not retroactively kill the creature.
            assertThat(perm.isDamagedByDeathtouch()).isFalse();
        }

        @Test
        @DisplayName("Regeneration replaces the deathtouch destruction")
        void regenerationReplacesDeathtouchDestruction() {
            Card card = createCreatureCard("Drudge Skeletons");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(1);
            perm.setDamagedByDeathtouch(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(1);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            doAnswer(invocation -> {
                perm.setMarkedDamage(0);
                return true;
            }).when(graveyardService).tryRegenerate(gd, perm);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
            assertThat(perm.isDamagedByDeathtouch()).isFalse();
        }
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
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Scornful Egotist is put into the graveyard (0 toughness).")));
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
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Grizzly Bears is destroyed (lethal damage).")));
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
        @DisplayName("A lethal-damage replacement may remove a later permanent during the scan")
        void lethalDamageReplacementCanMutateBattlefieldDuringScan() {
            Permanent creature = new Permanent(createCreatureCard("Protected Creature"));
            creature.setMarkedDamage(3);
            Permanent aura = new Permanent(new Card());
            gd.playerBattlefields.get(player1Id).add(creature);
            gd.playerBattlefields.get(player1Id).add(aura);

            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, creature)).thenReturn(2);
            when(gameQueryService.hasKeyword(gd, creature, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            doAnswer(invocation -> {
                creature.setMarkedDamage(0);
                gd.playerBattlefields.get(player1Id).remove(aura);
                return true;
            }).when(graveyardService).tryRegenerate(gd, creature);

            sut.performStateBasedActions(gd);

            assertThat(gd.playerBattlefields.get(player1Id)).containsExactly(creature);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, creature);
        }

        @Test
        @DisplayName("A replacement that mutates the battlefield does not skip a later lethal creature")
        void lethalDamageReplacementDoesNotSkipLaterLethalCreature() {
            Permanent protectedCreature = new Permanent(createCreatureCard("Protected Creature"));
            protectedCreature.setMarkedDamage(3);
            Permanent aura = new Permanent(new Card());
            Permanent doomedCreature = new Permanent(createCreatureCard("Doomed Creature"));
            doomedCreature.setMarkedDamage(3);
            gd.playerBattlefields.get(player1Id).add(protectedCreature);
            gd.playerBattlefields.get(player1Id).add(aura);
            gd.playerBattlefields.get(player1Id).add(doomedCreature);

            when(gameQueryService.isCreature(gd, protectedCreature)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, protectedCreature)).thenReturn(2);
            when(gameQueryService.hasKeyword(gd, protectedCreature, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(gameQueryService.isCreature(gd, aura)).thenReturn(false);
            when(gameQueryService.isCreature(gd, doomedCreature)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, doomedCreature)).thenReturn(2);
            when(gameQueryService.hasKeyword(gd, doomedCreature, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            doAnswer(invocation -> {
                protectedCreature.setMarkedDamage(0);
                gd.playerBattlefields.get(player1Id).remove(aura);
                return true;
            }).when(graveyardService).tryRegenerate(gd, protectedCreature);
            when(graveyardService.tryRegenerate(gd, doomedCreature)).thenReturn(false);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, doomedCreature);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, protectedCreature);
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
            doAnswer(invocation -> {
                perm.setMarkedDamage(0);
                return true;
            }).when(graveyardService).tryRegenerate(gd, perm);

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
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Enfeebled Creature is put into the graveyard (0 toughness).")));
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
            perm.setCounterCount(CounterType.LOYALTY, 0);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            when(gameQueryService.isPlaneswalker(gd, perm)).thenReturn(true);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Jace Beleren has no loyalty counters and is put into the graveyard.")));
        }

        @Test
        @DisplayName("Planeswalker with negative loyalty counters dies")
        void planeswalkerWithNegativeLoyaltyDies() {
            Card card = createPlaneswalkerCard("Chandra Nalaar");
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.LOYALTY, -1);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            when(gameQueryService.isPlaneswalker(gd, perm)).thenReturn(true);

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
            perm.setCounterCount(CounterType.LOYALTY, 0);
            gd.playerBattlefields.get(player1Id).add(perm);

            // isCreature true (animated planeswalker), but toughness is healthy
            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.isPlaneswalker(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(6);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
            // Death reason is captured at collection time: creature checks fail (healthy toughness,
            // no lethal damage), but planeswalker check succeeds (0 loyalty)
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("Gideon Jura has no loyalty counters and is put into the graveyard.")));
        }

        @Test
        @DisplayName("Planeswalker with positive loyalty counters survives")
        void planeswalkerWithPositiveLoyaltySurvives() {
            Card card = createPlaneswalkerCard("Liliana Vess");
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.LOYALTY, 3);
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

            verify(permanentRemovalService, atLeastOnce()).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Orphaned auras are removed when a planeswalker died")
        void orphanedAurasRemovedWhenPlaneswalkerDied() {
            Card card = createPlaneswalkerCard("Jace Beleren");
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.LOYALTY, 0);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(false);
            when(gameQueryService.isPlaneswalker(gd, perm)).thenReturn(true);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, atLeastOnce()).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Orphaned auras are swept even when nothing died")
        void orphanedAurasSweptWhenNothingDied() {
            // A host can leave the battlefield without anything dying — it phases out — so the
            // sweep can't hang off a death or those attachments are stranded until an
            // unrelated sweep happens to run.
            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, atLeastOnce()).removeOrphanedAuras(gd);
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
            perm.setCounterCount(CounterType.LORE, 3);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
            verify(gameLogService).append(eq(gd), argThat((GameLogEntry e) -> e.plainText().equals("The Eldest Reborn is sacrificed (final chapter reached).")));
        }

        @Test
        @DisplayName("Saga with more lore counters than final chapter is sacrificed")
        void sagaWithExcessLoreCountersIsSacrificed() {
            Card card = createSagaCard("History of Benalia", 3);
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.LORE, 5);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Saga with lore counters below final chapter is not sacrificed")
        void sagaBelowFinalChapterSurvives() {
            Card card = createSagaCard("Phyrexian Scriptures", 3);
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.LORE, 2);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, perm);
        }

        @Test
        @DisplayName("Saga is not sacrificed if chapter ability from it is still on the stack")
        void sagaNotSacrificedWhenChapterAbilityOnStack() {
            Card card = createSagaCard("The Flame of Keld", 3);
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.LORE, 3);
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
            perm.setCounterCount(CounterType.LORE, 3);
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
            perm.setCounterCount(CounterType.LORE, 3);
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
            perm.setCounterCount(CounterType.LORE, 5);
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
            perm.setCounterCount(CounterType.LORE, 10);
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
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        }

        @Test
        @DisplayName("Both counter types become zero when equal")
        void bothBecomeZeroWhenEqual() {
            Card card = createCreatureCard("Fertilid");
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        }

        @Test
        @DisplayName("More -1/-1 than +1/+1 leaves only -1/-1 counters")
        void moreMinusThanPlusLeavesMinusCounters() {
            Card card = createCreatureCard("Devoted Druid");
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 5);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        }

        @Test
        @DisplayName("No cancellation when only +1/+1 counters present")
        void noChangeWhenOnlyPlusCounters() {
            Card card = createCreatureCard("Slith Firewalker");
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
            assertThat(perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        }

        @Test
        @DisplayName("No cancellation when only -1/-1 counters present")
        void noChangeWhenOnlyMinusCounters() {
            Card card = createCreatureCard("Fading Creature");
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(perm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(perm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
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
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            // 3 counters cancelled => 3*2 = 6 pending regrowth counters
            assertThat(gd.getDelayedPlusOneCounters(perm.getId())).isEqualTo(6);
        }

        @Test
        @DisplayName("No delayed regrowth for permanents without the effect")
        void noRegrowthWithoutEffect() {
            Card card = createCreatureCard("Regular Creature");
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
            gd.playerBattlefields.get(player1Id).add(perm);

            sut.performStateBasedActions(gd);

            assertThat(gd.getDelayedPlusOneCounters(perm.getId())).isZero();
        }

        @Test
        @DisplayName("Regrowth stacks with existing pending counters")
        void regrowthStacksWithExisting() {
            Card card = createCreatureCard("Protean Hydra");
            card.addEffect(EffectSlot.STATIC, new DelayedPlusOnePlusOneCounterRegrowthEffect());
            Permanent perm = new Permanent(card);
            perm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
            perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
            gd.playerBattlefields.get(player1Id).add(perm);
            gd.addDelayedPlusOneCounters(perm.getId(), 4);

            sut.performStateBasedActions(gd);

            // existing 4 + (2 cancelled * 2) = 8
            assertThat(gd.getDelayedPlusOneCounters(perm.getId())).isEqualTo(8);
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
            when(gameOutcomeService.resolveLoss(gd, player1Id, LossReason.EMPTY_LIBRARY))
                    .thenReturn(LossOutcome.LOSES);
            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            sut.performStateBasedActions(gd);

            verify(gameOutcomeService).declareWinner(gd, player2Id);
            verify(gameLogService).append(eq(gd), eq(GameLog.text("Player1 attempted to draw from an empty library and loses the game.")));
        }

        @Test
        @DisplayName("playersAttemptedDrawFromEmptyLibrary is cleared after processing")
        void setIsClearedAfterProcessing() {
            gd.playersAttemptedDrawFromEmptyLibrary.add(player1Id);
            when(gameOutcomeService.resolveLoss(gd, player1Id, LossReason.EMPTY_LIBRARY))
                    .thenReturn(LossOutcome.LOSES);
            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            sut.performStateBasedActions(gd);

            assertThat(gd.playersAttemptedDrawFromEmptyLibrary).isEmpty();
        }

        @Test
        @DisplayName("Player with CantLoseGameEffect does not lose from empty library draw")
        void cantLosePlayerDoesNotLose() {
            gd.playersAttemptedDrawFromEmptyLibrary.add(player1Id);
            when(gameOutcomeService.resolveLoss(gd, player1Id, LossReason.EMPTY_LIBRARY))
                    .thenReturn(LossOutcome.PREVENTED);

            sut.performStateBasedActions(gd);

            verify(gameOutcomeService, never()).declareWinner(any(), any());
        }

        @Test
        @DisplayName("Set is cleared even when player cannot lose")
        void setIsClearedEvenWhenPlayerCannotLose() {
            gd.playersAttemptedDrawFromEmptyLibrary.add(player1Id);
            when(gameOutcomeService.resolveLoss(gd, player1Id, LossReason.EMPTY_LIBRARY))
                    .thenReturn(LossOutcome.PREVENTED);

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
            when(gameOutcomeService.resolveLoss(gd, player1Id, LossReason.EMPTY_LIBRARY))
                    .thenReturn(LossOutcome.LOSES);
            when(gameOutcomeService.resolveLoss(gd, player2Id, LossReason.EMPTY_LIBRARY))
                    .thenReturn(LossOutcome.LOSES);
            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);
            when(gameQueryService.getOpponentId(gd, player2Id)).thenReturn(player1Id);

            sut.performStateBasedActions(gd);

            verify(gameOutcomeService).declareWinner(gd, player2Id);
            verify(gameOutcomeService).declareWinner(gd, player1Id);
            assertThat(gd.playersAttemptedDrawFromEmptyLibrary).isEmpty();
        }

        @Test
        @DisplayName("A replaced loss does not finish the game")
        void replacedLossDoesNotFinishTheGame() {
            gd.playersAttemptedDrawFromEmptyLibrary.add(player1Id);
            when(gameOutcomeService.resolveLoss(gd, player1Id, LossReason.EMPTY_LIBRARY))
                    .thenReturn(LossOutcome.REPLACED);

            sut.performStateBasedActions(gd);

            // This site used to consult only the can't-lose effects, so a Lich's Mirror reset
            // never got a say and the game ended anyway.
            verify(gameOutcomeService, never()).declareWinner(any(), any());
        }

        @Test
        @DisplayName("A replacement that re-arms the flag leaves it armed for the next check")
        void reArmedFlagSurvivesTheCheckThatReplacedIt() {
            gd.playersAttemptedDrawFromEmptyLibrary.add(player1Id);
            // Lich's Mirror's seven-card draw can run the library dry again, re-arming the flag
            // mid-check. Clearing the whole set afterwards would swallow that second loss.
            doAnswer(invocation -> {
                gd.playersAttemptedDrawFromEmptyLibrary.add(player1Id);
                return LossOutcome.REPLACED;
            }).when(gameOutcomeService).resolveLoss(gd, player1Id, LossReason.EMPTY_LIBRARY);

            sut.performStateBasedActions(gd);

            verify(gameOutcomeService, never()).declareWinner(any(), any());
            assertThat(gd.playersAttemptedDrawFromEmptyLibrary).contains(player1Id);
        }
    }

    @Nested
    @DisplayName("Fixpoint repetition — CR 704.3/704.4")
    class FixpointRepetition {

        @Test
        @DisplayName("Creature whose damage becomes lethal after an anthem source dies is destroyed in the same check")
        void reChecksLethalDamageAfterAnthemSourceDies() {
            Card lordCard = createCreatureCard("Lord");
            Permanent lord = new Permanent(lordCard);
            lord.setMarkedDamage(2);
            Card bearCard = createCreatureCard("Bear");
            Permanent bear = new Permanent(bearCard);
            bear.setMarkedDamage(2);
            List<Permanent> battlefield = gd.playerBattlefields.get(player1Id);
            battlefield.add(lord);
            battlefield.add(bear);

            when(gameQueryService.isCreature(gd, lord)).thenReturn(true);
            when(gameQueryService.isCreature(gd, bear)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, lord)).thenReturn(2);
            // The bear is 2/3 while the lord's anthem applies, 2/2 once the lord is gone
            when(gameQueryService.getEffectiveToughness(gd, bear))
                    .thenAnswer(inv -> battlefield.contains(lord) ? 3 : 2);
            when(gameQueryService.hasKeyword(eq(gd), any(), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);
            when(graveyardService.tryRegenerate(eq(gd), any())).thenReturn(false);
            doAnswer(inv -> {
                battlefield.remove((Permanent) inv.getArgument(1));
                return null;
            }).when(permanentRemovalService).removePermanentToGraveyard(eq(gd), any());

            sut.performStateBasedActions(gd);

            // First pass: bear survives (2 damage < 3 toughness), lord dies. Second pass: the
            // bear's marked damage is now lethal (2 >= 2) and it must die before anything else.
            verify(permanentRemovalService).removePermanentToGraveyard(gd, lord);
            verify(permanentRemovalService).removePermanentToGraveyard(gd, bear);
        }

        @Test
        @DisplayName("Attachment legality is enforced as part of every SBA check")
        void attachmentLegalityEnforcedEachCheck() {
            sut.performStateBasedActions(gd);

            verify(permanentRemovalService).enforceAttachmentLegality(gd);
        }

        @Test
        @DisplayName("An attachment change triggers another SBA pass")
        void attachmentChangeTriggersAnotherPass() {
            when(permanentRemovalService.enforceAttachmentLegality(gd)).thenReturn(true, false);

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, times(2)).enforceAttachmentLegality(gd);
        }

        @Test
        @DisplayName("Permanent that stays on the battlefield after removal is not processed twice")
        void deadPermanentNotProcessedTwice() {
            Card card = createCreatureCard("Stubborn Creature");
            Permanent perm = new Permanent(card);
            perm.setMarkedDamage(5);
            gd.playerBattlefields.get(player1Id).add(perm);

            when(gameQueryService.isCreature(gd, perm)).thenReturn(true);
            when(gameQueryService.getEffectiveToughness(gd, perm)).thenReturn(2);
            when(gameQueryService.hasKeyword(gd, perm, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, perm)).thenReturn(false);
            // The mocked removal leaves the permanent on the battlefield; the repeat loop
            // must not pick it up again.

            sut.performStateBasedActions(gd);

            verify(permanentRemovalService, times(1)).removePermanentToGraveyard(gd, perm);
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

    @Nested
    @DisplayName("Legend rule — CR 704.5j")
    class LegendRule {

        @Test
        @DisplayName("Checks the legend rule for every player once other actions settle")
        void checksLegendRuleForEveryPlayer() {
            sut.performStateBasedActions(gd);

            verify(legendRuleService).checkLegendRule(gd, player1Id);
            verify(legendRuleService).checkLegendRule(gd, player2Id);
        }

        @Test
        @DisplayName("Stops before state triggers when a legend-rule choice is prompted")
        void stopsWhenLegendRulePrompts() {
            when(legendRuleService.checkLegendRule(gd, player1Id)).thenReturn(true);

            sut.performStateBasedActions(gd);

            verify(legendRuleService, never()).checkLegendRule(gd, player2Id);
            verify(stateTriggerService, never()).checkStateTriggers(gd);
        }

        @Test
        @DisplayName("Defers the legend-rule check while another interaction is active")
        void defersWhileInteractionActive() {
            gd.interaction.beginInteraction(new com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice(
                    player1Id, List.of(), List.of(), null, "unrelated choice"));

            sut.performStateBasedActions(gd);

            verify(legendRuleService, never()).checkLegendRule(any(), any());
        }

        @Test
        @DisplayName("Defers the legend-rule check while other interactions are queued")
        void defersWhileInteractionsQueued() {
            gd.queueInteraction(new com.github.laxika.magicalvibes.model.PendingInteraction.PermanentChoice(
                    player1Id, List.of(), List.of(), null, "queued choice"));

            sut.performStateBasedActions(gd);

            verify(legendRuleService, never()).checkLegendRule(any(), any());
        }
    }

    @Nested
    @DisplayName("Cards exiled with a permanent its controller lost control of — Gustha's Scepter")
    class ExiledCardsOnControlLoss {

        private Card exiledCard;
        private UUID scepterId;

        @BeforeEach
        void setUpWatch() {
            exiledCard = createCreatureCard("Grizzly Bears");
            scepterId = UUID.randomUUID();
            gd.addToExile(player1Id, exiledCard, scepterId, true);
            gd.exiledCardsToGraveyardOnControlLossWatch.put(scepterId, player1Id);
        }

        @Test
        @DisplayName("Exiled cards are put into their owner's graveyard when the source leaves the battlefield")
        void sourceLeftBattlefield() {
            sut.performStateBasedActions(gd);

            verify(graveyardService).addCardToGraveyard(gd, player1Id, exiledCard);
            assertThat(gd.exiledCards).isEmpty();
            assertThat(gd.exiledCardsToGraveyardOnControlLossWatch).isEmpty();
        }

        @Test
        @DisplayName("Exiled cards are put into their owner's graveyard when another player gains control of the source")
        void controllerChanged() {
            Card artifact = new Card();
            artifact.setName("Gustha's Scepter");
            artifact.setType(CardType.ARTIFACT);
            Permanent scepter = new Permanent(artifact);
            gd.playerBattlefields.get(player2Id).add(scepter);
            when(gameQueryService.findPermanentById(gd, scepterId)).thenReturn(scepter);

            sut.performStateBasedActions(gd);

            verify(graveyardService).addCardToGraveyard(gd, player1Id, exiledCard);
            assertThat(gd.exiledCards).isEmpty();
            assertThat(gd.exiledCardsToGraveyardOnControlLossWatch).isEmpty();
        }

        @Test
        @DisplayName("Exiled cards stay in exile while the same player still controls the source")
        void controllerUnchanged() {
            Card artifact = new Card();
            artifact.setName("Gustha's Scepter");
            artifact.setType(CardType.ARTIFACT);
            Permanent scepter = new Permanent(artifact);
            gd.playerBattlefields.get(player1Id).add(scepter);
            when(gameQueryService.findPermanentById(gd, scepterId)).thenReturn(scepter);

            sut.performStateBasedActions(gd);

            verify(graveyardService, never()).addCardToGraveyard(any(), any(), any());
            assertThat(gd.exiledCards).hasSize(1);
            assertThat(gd.exiledCardsToGraveyardOnControlLossWatch).containsEntry(scepterId, player1Id);
        }
    }
}
