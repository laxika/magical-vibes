package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.BoostColorSourceDamageThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfFewCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetControllerIfTargetHasKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DealOrderedDamageToAnyTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetAndGainXLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DamageResolutionServiceTest {

    @Mock
    private GraveyardService graveyardService;

    @Mock
    private DamagePreventionService damagePreventionService;

    @Mock
    private GameOutcomeService gameOutcomeService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @Mock
    private TriggerCollectionService triggerCollectionService;

    @Mock
    private LifeResolutionService lifeResolutionService;

    @InjectMocks
    private DamageResolutionService drs;

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
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerLifeTotals.put(player1Id, 20);
        gd.playerLifeTotals.put(player2Id, 20);
    }

    // ===== Helper methods =====

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        return card;
    }

    private static Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Permanent addPermanent(UUID playerId, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(permanent);
        return permanent;
    }

    private StackEntry createEntry(Card card, UUID controllerId, UUID targetId) {
        StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId, card.getName(), List.of());
        entry.setTargetId(targetId);
        return entry;
    }

    private StackEntry createEntryWithXValue(Card card, UUID controllerId, int xValue, UUID targetId) {
        StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), List.of(), xValue);
        entry.setTargetId(targetId);
        return entry;
    }

    private StackEntry createMultiTargetEntry(Card card, UUID controllerId, List<UUID> targetIds) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId, card.getName(), List.of(), 0, targetIds);
    }

    /**
     * Stubs the damage prevention system as active (isDamagePreventable = true).
     * Used by resolveAnyTargetDamage, isDamagePreventedForCreature, isDamageSourcePreventedWithLog, etc.
     */
    private void stubDamagePreventable() {
        when(gameQueryService.isDamagePreventable(gd)).thenReturn(true);
    }

    /**
     * Stubs that no global source-color prevention is active.
     * Used by isDamageSourcePreventedWithLog and isDamagePreventedForCreature.
     */
    private void stubDamageFromSourceNotPrevented() {
        when(gameQueryService.isDamageFromSourcePrevented(eq(gd), any())).thenReturn(false);
    }

    /**
     * Stubs the damage multiplier to pass through unchanged.
     */
    private void stubNoDamageMultiplier() {
        when(gameQueryService.applyDamageMultiplier(eq(gd), anyInt(), any(StackEntry.class))).thenAnswer(inv -> inv.getArgument(1));
    }

    /**
     * Core stubs for dealCreatureDamage when sourcePermanentId is null (spell entries).
     * Stubs findPermanentController, applyCreaturePreventionShield (passthrough), and getEffectiveToughness.
     * Does NOT stub applySourceRedirectShields or applyTargetSourcePreventionShield (gated by null sourcePermId).
     */
    private void stubCreatureDamageCore(Permanent target, int toughness) {
        when(gameQueryService.findPermanentController(eq(gd), eq(target.getId()))).thenReturn(player2Id);
        when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(target), anyInt())).thenAnswer(inv -> inv.getArgument(2));
        when(gameQueryService.getEffectiveToughness(gd, target)).thenReturn(toughness);
    }

    /**
     * Stubs source-specific redirect and target-source prevention shields for creature damage.
     * Only needed when sourcePermanentId is non-null (bite/fight, activated abilities from permanents).
     */
    private void stubCreatureSourceRedirects() {
        when(damagePreventionService.applySourceRedirectShields(eq(gd), any(), any(), anyInt())).thenAnswer(inv -> inv.getArgument(3));
        when(damagePreventionService.applyTargetSourcePreventionShield(eq(gd), any(), any(), anyInt())).thenAnswer(inv -> inv.getArgument(3));
    }

    /**
     * Stubs isLethalDamage to return the given value for any inputs.
     */
    private void stubLethalDamage(boolean isLethal) {
        when(gameQueryService.isLethalDamage(anyInt(), anyInt(), anyBoolean())).thenReturn(isLethal);
    }

    /**
     * Stubs that the source has neither infect nor deathtouch (null damageSource variant).
     * Used for creature damage paths where both keywords are checked.
     */
    private void stubNoKeywordsOnSource(StackEntry entry) {
        when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), isNull(), eq(Keyword.INFECT))).thenReturn(false);
        when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), isNull(), eq(Keyword.DEATHTOUCH))).thenReturn(false);
    }

    /**
     * Stubs that the damage source permanent has neither infect nor deathtouch.
     * Used for bite/fight paths where the damage source is a specific permanent.
     */
    private void stubNoKeywordsOnSourceWithDamageSource(StackEntry entry, Permanent damageSource) {
        when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), eq(damageSource), eq(Keyword.INFECT))).thenReturn(false);
        when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), eq(damageSource), eq(Keyword.DEATHTOUCH))).thenReturn(false);
    }

    /**
     * Stubs that the source has no infect (null damageSource).
     * Used for player-only damage paths where only infect is checked (deathtouch is not).
     */
    private void stubNoInfectOnSource(StackEntry entry) {
        when(gameQueryService.sourceHasKeyword(eq(gd), eq(entry), isNull(), eq(Keyword.INFECT))).thenReturn(false);
    }

    /**
     * Core stubs for dealDamageToPlayer when sourcePermanentId is null.
     * Stubs all prevention/reduction services as passthrough, canPlayerLifeChange = true,
     * shouldDamageBeDealtAsInfect = false.
     * Does NOT include sourceHasKeyword(INFECT) — use stubNoInfectOnSource or stubNoKeywordsOnSource separately.
     */
    private void stubPlayerDamageCore(UUID playerId) {
        when(damagePreventionService.isSourceDamagePreventedForPlayer(eq(gd), eq(playerId), any())).thenReturn(false);
        when(damagePreventionService.applySourceRedirectShields(eq(gd), eq(playerId), any(), anyInt())).thenAnswer(inv -> inv.getArgument(3));
        when(damagePreventionService.applyColorDamagePreventionForPlayer(eq(gd), eq(playerId), any())).thenReturn(false);
        when(damagePreventionService.applyOpponentSourceDamageReduction(eq(gd), eq(playerId), any(), anyInt())).thenAnswer(inv -> inv.getArgument(3));
        when(damagePreventionService.applyPlayerPreventionShield(eq(gd), eq(playerId), anyInt())).thenAnswer(inv -> inv.getArgument(2));
        when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(playerId), anyInt(), anyString())).thenAnswer(inv -> inv.getArgument(2));
        when(gameQueryService.canPlayerLifeChange(gd, playerId)).thenReturn(true);
        when(gameQueryService.shouldDamageBeDealtAsInfect(gd, playerId)).thenReturn(false);
    }

    // =========================================================================
    // DealDamageToAnyTargetEffect — creature target
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToAnyTarget")
    class ResolveDealDamageToAnyTarget {

        @Test
        @DisplayName("Deals lethal damage to a creature and destroys it")
        void dealsLethalDamageToCreatureAndDestroysIt() {
            Card shockCard = createCard("Shock");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntry(shockCard, player1Id, bears.getId());
            DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(2, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

            drs.resolveDealDamageToAnyTarget(gd, entry, effect);

            assertThat(bears.getMarkedDamage()).isEqualTo(2);
            assertThat(gd.pendingLethalDamageDestructions).contains(bears);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
        }

        @Test
        @DisplayName("Deals non-lethal damage to a creature and it survives")
        void dealsNonLethalDamageToCreature() {
            Card shockCard = createCard("Shock");
            Permanent angel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
            StackEntry entry = createEntry(shockCard, player1Id, angel.getId());
            DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(2, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(angel, 4);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(false);
            when(gameQueryService.findPermanentById(gd, angel.getId())).thenReturn(angel);

            drs.resolveDealDamageToAnyTarget(gd, entry, effect);

            assertThat(angel.getMarkedDamage()).isEqualTo(2);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, angel, 2, player1Id);
        }

        @Test
        @DisplayName("Deals damage to a player and reduces their life total")
        void dealsDamageToPlayer() {
            Card shockCard = createCard("Shock");
            StackEntry entry = createEntry(shockCard, player1Id, player2Id);
            DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(2, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            drs.resolveDealDamageToAnyTarget(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(18);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 2);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }

        @Test
        @DisplayName("Does nothing when targetId is null")
        void doesNothingWhenTargetNull() {
            Card shockCard = createCard("Shock");
            StackEntry entry = createEntry(shockCard, player1Id, null);
            DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(2, false);

            drs.resolveDealDamageToAnyTarget(gd, entry, effect);

            verify(gameQueryService, never()).applyDamageMultiplier(any(), anyInt(), any());
            verifyNoInteractions(triggerCollectionService);
        }

        @Test
        @DisplayName("Damage is logged via broadcast service")
        void damageIsLogged() {
            Card shockCard = createCard("Shock");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntry(shockCard, player1Id, bears.getId());
            DealDamageToAnyTargetEffect effect = new DealDamageToAnyTargetEffect(2, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

            drs.resolveDealDamageToAnyTarget(gd, entry, effect);

            verify(gameBroadcastService, atLeastOnce()).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Shock") && msg.contains("2 damage") && msg.contains("Grizzly Bears")));
        }
    }

    // =========================================================================
    // DealDamageToTargetCreatureEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToTargetCreature")
    class ResolveDealDamageToTargetCreature {

        @Test
        @DisplayName("Deals damage to a creature and destroys it")
        void dealsDamageToCreature() {
            Card burnCard = createCard("Burn the Impure");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntry(burnCard, player1Id, bears.getId());
            DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(3, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

            drs.resolveDealDamageToTargetCreature(gd, entry, effect);

            assertThat(bears.getMarkedDamage()).isEqualTo(3);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 3, player1Id);
        }

        @Test
        @DisplayName("Tracks creature in permanentsDealtDamageThisTurn when damage is dealt")
        void tracksPermanentDealtDamageThisTurn() {
            Card burnCard = createCard("Shock");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntry(burnCard, player1Id, bears.getId());
            DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(1, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(false);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

            drs.resolveDealDamageToTargetCreature(gd, entry, effect);

            assertThat(gd.permanentsDealtDamageThisTurn).contains(bears.getId());
        }

        @Test
        @DisplayName("Does not track creature in permanentsDealtDamageThisTurn when damage is fully prevented")
        void doesNotTrackWhenDamageFullyPrevented() {
            Card burnCard = createCard("Shock");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntry(burnCard, player1Id, bears.getId());
            DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(1, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            when(gameQueryService.findPermanentController(eq(gd), eq(bears.getId()))).thenReturn(player2Id);
            // Prevention shield reduces damage to 0
            when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(bears), anyInt())).thenReturn(0);
            when(gameQueryService.getEffectiveToughness(gd, bears)).thenReturn(2);
            stubNoKeywordsOnSource(entry);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

            drs.resolveDealDamageToTargetCreature(gd, entry, effect);

            assertThat(gd.permanentsDealtDamageThisTurn).doesNotContain(bears.getId());
        }

        @Test
        @DisplayName("Multi-target: deals damage to each creature in targetIds")
        void multiTargetDealsDamageToEachCreature() {
            Card burnCard = createCard("Dual Shot");
            Permanent bear1 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            Permanent bear2 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createMultiTargetEntry(burnCard, player1Id, List.of(bear1.getId(), bear2.getId()));
            DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(1, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bear1, 2);
            stubCreatureDamageCore(bear2, 2);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(false);
            when(gameQueryService.findPermanentById(gd, bear1.getId())).thenReturn(bear1);
            when(gameQueryService.findPermanentById(gd, bear2.getId())).thenReturn(bear2);
            when(gameQueryService.hasProtectionFromSource(eq(gd), any(Permanent.class), any(Card.class))).thenReturn(false);

            drs.resolveDealDamageToTargetCreature(gd, entry, effect);

            assertThat(bear1.getMarkedDamage()).isEqualTo(1);
            assertThat(bear2.getMarkedDamage()).isEqualTo(1);
        }

        @Test
        @DisplayName("Multi-target: skips removed targets and damages remaining ones")
        void multiTargetSkipsRemovedTargets() {
            Card burnCard = createCard("Dual Shot");
            Permanent bear1 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            Permanent bear2 = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            UUID removedId = bear1.getId();
            StackEntry entry = createMultiTargetEntry(burnCard, player1Id, List.of(removedId, bear2.getId()));
            DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(1, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bear2, 2);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(false);
            // bear1 was removed from battlefield before resolution
            when(gameQueryService.findPermanentById(gd, removedId)).thenReturn(null);
            when(gameQueryService.findPermanentById(gd, bear2.getId())).thenReturn(bear2);
            when(gameQueryService.hasProtectionFromSource(eq(gd), any(Permanent.class), any(Card.class))).thenReturn(false);

            drs.resolveDealDamageToTargetCreature(gd, entry, effect);

            assertThat(bear2.getMarkedDamage()).isEqualTo(1);
        }

        @Test
        @DisplayName("Single targetIds entry with primary targetId uses single-target path")
        void singleTargetIdsWithPrimaryTargetUsesSingleTargetPath() {
            Card burnCard = createCard("Goblin Barrage");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            // Kicked spell: creature in targetId, player in targetIds (size 1)
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, burnCard, player1Id, burnCard.getName(),
                    List.of(), 0, bears.getId(), null, Map.of(), null, List.of(), List.of(player2Id));
            DealDamageToTargetCreatureEffect effect = new DealDamageToTargetCreatureEffect(4, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

            drs.resolveDealDamageToTargetCreature(gd, entry, effect);

            assertThat(bears.getMarkedDamage()).isEqualTo(4);
        }
    }

    // =========================================================================
    // DealDamageToTargetControllerIfTargetHasKeywordEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToTargetControllerIfTargetHasKeyword")
    class ResolveDealDamageToTargetControllerIfTargetHasKeyword {

        @Test
        @DisplayName("Deals bonus damage to controller when target creature has the keyword")
        void dealsBonusDamageWhenTargetHasKeyword() {
            Card burnCard = createCard("Burn the Impure");
            Permanent creature = addPermanent(player2Id, createCreature("Blightwidow", 2, 4));
            StackEntry entry = createEntry(burnCard, player1Id, creature.getId());
            DealDamageToTargetControllerIfTargetHasKeywordEffect effect =
                    new DealDamageToTargetControllerIfTargetHasKeywordEffect(3, Keyword.INFECT);

            stubNoDamageMultiplier();
            stubDamageFromSourceNotPrevented();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);
            when(gameQueryService.findPermanentById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.hasKeyword(gd, creature, Keyword.INFECT)).thenReturn(true);
            when(gameQueryService.findPermanentController(gd, creature.getId())).thenReturn(player2Id);

            drs.resolveDealDamageToTargetControllerIfTargetHasKeyword(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 3);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }

        @Test
        @DisplayName("Does not deal bonus damage when target creature lacks the keyword")
        void noBonusDamageWhenTargetLacksKeyword() {
            Card burnCard = createCard("Burn the Impure");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntry(burnCard, player1Id, bears.getId());
            DealDamageToTargetControllerIfTargetHasKeywordEffect effect =
                    new DealDamageToTargetControllerIfTargetHasKeywordEffect(3, Keyword.INFECT);

            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INFECT)).thenReturn(false);

            drs.resolveDealDamageToTargetControllerIfTargetHasKeyword(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            verifyNoInteractions(triggerCollectionService);
        }
    }

    // =========================================================================
    // DealDamageToTargetPlayerEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToTargetPlayer")
    class ResolveDealDamageToTargetPlayer {

        @Test
        @DisplayName("Deals damage to target player")
        void dealsDamageToTargetPlayer() {
            Card lavaAxeCard = createCard("Lava Axe");
            StackEntry entry = createEntry(lavaAxeCard, player1Id, player2Id);
            DealDamageToTargetPlayerEffect effect = new DealDamageToTargetPlayerEffect(5);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            drs.resolveDealDamageToTargetPlayer(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 5);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }

        @Test
        @DisplayName("Does nothing when target is not a player")
        void doesNothingWhenTargetNotPlayer() {
            Card lavaAxeCard = createCard("Lava Axe");
            UUID fakeId = UUID.randomUUID();
            StackEntry entry = createEntry(lavaAxeCard, player1Id, fakeId);
            DealDamageToTargetPlayerEffect effect = new DealDamageToTargetPlayerEffect(5);

            drs.resolveDealDamageToTargetPlayer(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            verifyNoInteractions(triggerCollectionService);
        }
    }

    // =========================================================================
    // DealDamageToControllerEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToController")
    class ResolveDealDamageToController {

        @Test
        @DisplayName("Deals damage to the controller of the ability")
        void dealsDamageToController() {
            Card artilleryCard = createCard("Orcish Artillery");
            StackEntry entry = createEntry(artilleryCard, player1Id, null);
            DealDamageToControllerEffect effect = new DealDamageToControllerEffect(3);

            stubNoDamageMultiplier();
            stubDamageFromSourceNotPrevented();
            stubPlayerDamageCore(player1Id);
            stubNoInfectOnSource(entry);

            drs.resolveDealDamageToController(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(17);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player1Id, 3);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player1Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player1Id);
        }
    }

    // =========================================================================
    // MassDamageEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveMassDamage")
    class ResolveMassDamage {

        @Test
        @DisplayName("Deals damage to all creatures and kills those with toughness <= damage")
        void damagesAllCreatures() {
            Card pyroCard = createCard("Pyroclasm");
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2));
            Permanent elves = addPermanent(player2Id, createCreature("Llanowar Elves", 1, 1));
            StackEntry entry = createEntry(pyroCard, player1Id, null);
            MassDamageEffect effect = new MassDamageEffect(2);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);
            // Inline creature stubs — controllers differ from the default player2Id
            when(gameQueryService.findPermanentController(eq(gd), eq(bears.getId()))).thenReturn(player1Id);
            when(gameQueryService.findPermanentController(eq(gd), eq(elves.getId()))).thenReturn(player2Id);
            when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(bears), anyInt())).thenAnswer(inv -> inv.getArgument(2));
            when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(elves), anyInt())).thenAnswer(inv -> inv.getArgument(2));
            when(gameQueryService.getEffectiveToughness(gd, bears)).thenReturn(2);
            when(gameQueryService.getEffectiveToughness(gd, elves)).thenReturn(1);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(true);
            when(gameQueryService.hasKeyword(eq(gd), any(Permanent.class), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);
            when(graveyardService.tryRegenerate(eq(gd), any(Permanent.class))).thenReturn(false);

            drs.resolveMassDamage(gd, entry, effect);

            assertThat(bears.getMarkedDamage()).isEqualTo(2);
            assertThat(elves.getMarkedDamage()).isEqualTo(2);
            verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
            verify(permanentRemovalService).removePermanentToGraveyard(gd, elves);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, elves, 2, player1Id);
        }

        @Test
        @DisplayName("Does not kill creatures with toughness > damage")
        void doesNotKillHighToughnessCreatures() {
            Card pyroCard = createCard("Pyroclasm");
            Permanent angel = addPermanent(player1Id, createCreature("Serra Angel", 4, 4));
            StackEntry entry = createEntry(pyroCard, player1Id, null);
            MassDamageEffect effect = new MassDamageEffect(2);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);
            // Inline creature stubs — controller is player1Id, not the default player2Id
            when(gameQueryService.findPermanentController(eq(gd), eq(angel.getId()))).thenReturn(player1Id);
            when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(angel), anyInt())).thenAnswer(inv -> inv.getArgument(2));
            when(gameQueryService.getEffectiveToughness(gd, angel)).thenReturn(4);
            stubNoKeywordsOnSource(entry);
            when(gameQueryService.isLethalDamage(2, 4, false)).thenReturn(false);

            drs.resolveMassDamage(gd, entry, effect);

            assertThat(angel.getMarkedDamage()).isEqualTo(2);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, angel, 2, player1Id);
        }

        @Test
        @DisplayName("Deals X damage to creatures matching filter and to all players when damagesPlayers is true")
        void hurricaneDealsXDamageToFilteredCreaturesAndPlayers() {
            Card hurricaneCard = createCard("Hurricane");
            hurricaneCard.setColor(CardColor.GREEN);
            Permanent angel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntryWithXValue(hurricaneCard, player1Id, 4, null);
            MassDamageEffect effect = new MassDamageEffect(0, true, true, null);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);
            // Inline creature stubs — controllers differ from the default player2Id
            when(gameQueryService.findPermanentController(eq(gd), eq(angel.getId()))).thenReturn(player2Id);
            when(gameQueryService.findPermanentController(eq(gd), eq(bears.getId()))).thenReturn(player1Id);
            when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(angel), anyInt())).thenAnswer(inv -> inv.getArgument(2));
            when(damagePreventionService.applyCreaturePreventionShield(eq(gd), eq(bears), anyInt())).thenAnswer(inv -> inv.getArgument(2));
            when(gameQueryService.getEffectiveToughness(gd, angel)).thenReturn(4);
            when(gameQueryService.getEffectiveToughness(gd, bears)).thenReturn(2);
            stubNoKeywordsOnSource(entry);
            when(gameQueryService.isLethalDamage(4, 4, false)).thenReturn(true);
            when(gameQueryService.isLethalDamage(4, 2, false)).thenReturn(true);
            when(gameQueryService.hasKeyword(eq(gd), any(Permanent.class), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);
            when(graveyardService.tryRegenerate(eq(gd), any(Permanent.class))).thenReturn(false);
            stubPlayerDamageCore(player1Id);
            stubPlayerDamageCore(player2Id);

            drs.resolveMassDamage(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player1Id)).isEqualTo(16);
            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(16);
            verify(gameOutcomeService).checkWinCondition(gd);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, angel, 4, player1Id);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 4, player1Id);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player1Id, 4);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 4);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player1Id, null, false);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player1Id);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }
    }

    // =========================================================================
    // DealXDamageToAnyTargetEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDealXDamageToAnyTarget")
    class ResolveDealXDamageToAnyTarget {

        @Test
        @DisplayName("Deals X damage to a creature and destroys it")
        void dealsXDamageToCreature() {
            Card blazeCard = createCard("Blaze");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntryWithXValue(blazeCard, player1Id, 3, bears.getId());
            DealXDamageToAnyTargetEffect effect = new DealXDamageToAnyTargetEffect(false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

            drs.resolveDealXDamageToAnyTarget(gd, entry, effect);

            assertThat(bears.getMarkedDamage()).isEqualTo(3);
            assertThat(gd.pendingLethalDamageDestructions).contains(bears);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 3, player1Id);
        }

        @Test
        @DisplayName("Deals X damage to a player")
        void dealsXDamageToPlayer() {
            Card blazeCard = createCard("Blaze");
            StackEntry entry = createEntryWithXValue(blazeCard, player1Id, 5, player2Id);
            DealXDamageToAnyTargetEffect effect = new DealXDamageToAnyTargetEffect(false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            drs.resolveDealXDamageToAnyTarget(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 5);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }
    }

    // =========================================================================
    // DealDamageToAnyTargetAndGainLifeEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToAnyTargetAndGainLife")
    class ResolveDealDamageToAnyTargetAndGainLife {

        @Test
        @DisplayName("Deals damage to target player and controller gains life")
        void dealsDamageAndGainsLife() {
            Card drainCard = createCard("Essence Drain");
            drainCard.setColor(CardColor.BLACK);
            StackEntry entry = createEntry(drainCard, player1Id, player2Id);
            DealDamageToAnyTargetAndGainLifeEffect effect = new DealDamageToAnyTargetAndGainLifeEffect(3, 3);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            drs.resolveDealDamageToAnyTargetAndGainLife(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
            verify(lifeResolutionService).applyGainLife(gd, player1Id, 3);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 3);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }

        @Test
        @DisplayName("Deals damage to creature and controller gains life")
        void dealsDamageToCreatureAndGainsLife() {
            Card drainCard = createCard("Essence Drain");
            drainCard.setColor(CardColor.BLACK);
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntry(drainCard, player1Id, bears.getId());
            DealDamageToAnyTargetAndGainLifeEffect effect = new DealDamageToAnyTargetAndGainLifeEffect(3, 3);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

            drs.resolveDealDamageToAnyTargetAndGainLife(gd, entry, effect);

            assertThat(gd.pendingLethalDamageDestructions).contains(bears);
            verify(lifeResolutionService).applyGainLife(gd, player1Id, 3);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 3, player1Id);
        }
    }

    // =========================================================================
    // DealXDamageToAnyTargetAndGainXLifeEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDealXDamageToAnyTargetAndGainXLife")
    class ResolveDealXDamageToAnyTargetAndGainXLife {

        @Test
        @DisplayName("Deals X damage and gains X life")
        void dealsXDamageAndGainsXLife() {
            Card consumeCard = createCard("Consume Spirit");
            consumeCard.setColor(CardColor.BLACK);
            StackEntry entry = createEntryWithXValue(consumeCard, player1Id, 3, player2Id);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            drs.resolveDealXDamageToAnyTargetAndGainXLife(gd, entry);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
            verify(lifeResolutionService).applyGainLife(gd, player1Id, 3);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 3);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }
    }

    // =========================================================================
    // DealDamageToTargetPlayerByHandSizeEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToTargetPlayerByHandSize")
    class ResolveDealDamageToTargetPlayerByHandSize {

        @Test
        @DisplayName("Deals damage equal to target player's hand size")
        void dealsDamageEqualToHandSize() {
            Card impactCard = createCard("Sudden Impact");
            StackEntry entry = createEntry(impactCard, player1Id, player2Id);

            // Give player2 a hand of 5 cards
            for (int i = 0; i < 5; i++) {
                gd.playerHands.get(player2Id).add(createCreature("Bear " + i, 2, 2));
            }

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            drs.resolveDealDamageToTargetPlayerByHandSize(gd, entry);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(15);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 5);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }

        @Test
        @DisplayName("Deals 0 damage when target has an empty hand")
        void dealsZeroDamageWhenEmptyHand() {
            Card impactCard = createCard("Sudden Impact");
            StackEntry entry = createEntry(impactCard, player1Id, player2Id);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            // dealDamageToPlayer is called with rawDamage=0, early-returns after applySourceRedirectShields
            when(damagePreventionService.isSourceDamagePreventedForPlayer(eq(gd), eq(player2Id), any())).thenReturn(false);
            when(damagePreventionService.applySourceRedirectShields(eq(gd), eq(player2Id), any(), anyInt())).thenAnswer(inv -> inv.getArgument(3));

            drs.resolveDealDamageToTargetPlayerByHandSize(gd, entry);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            verifyNoInteractions(triggerCollectionService);
        }
    }

    // =========================================================================
    // DealDamageIfFewCardsInHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageIfFewCardsInHand")
    class ResolveDealDamageIfFewCardsInHand {

        @Test
        @DisplayName("Deals damage when opponent has cards <= maxCards")
        void dealsDamageWhenOpponentHasFewCards() {
            Card museCard = createCard("Lavaborn Muse");
            StackEntry entry = createEntry(museCard, player1Id, player2Id);
            DealDamageIfFewCardsInHandEffect effect = new DealDamageIfFewCardsInHandEffect(2, 3);

            // Player2 has 2 cards (at the threshold)
            gd.playerHands.get(player2Id).add(createCreature("Bear1", 2, 2));
            gd.playerHands.get(player2Id).add(createCreature("Bear2", 2, 2));

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            drs.resolveDealDamageIfFewCardsInHand(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 3);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }

        @Test
        @DisplayName("Deals damage when opponent has 0 cards in hand")
        void dealsDamageWhenOpponentHasEmptyHand() {
            Card museCard = createCard("Lavaborn Muse");
            StackEntry entry = createEntry(museCard, player1Id, player2Id);
            DealDamageIfFewCardsInHandEffect effect = new DealDamageIfFewCardsInHandEffect(2, 3);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubPlayerDamageCore(player2Id);
            stubNoInfectOnSource(entry);

            drs.resolveDealDamageIfFewCardsInHand(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(17);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 3);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }

        @Test
        @DisplayName("Does nothing when opponent has more than maxCards")
        void doesNothingWhenOpponentHasTooManyCards() {
            Card museCard = createCard("Lavaborn Muse");
            StackEntry entry = createEntry(museCard, player1Id, player2Id);
            DealDamageIfFewCardsInHandEffect effect = new DealDamageIfFewCardsInHandEffect(2, 3);

            // Player2 has 3 cards (exceeds maxCards of 2)
            gd.playerHands.get(player2Id).add(createCreature("Bear1", 2, 2));
            gd.playerHands.get(player2Id).add(createCreature("Bear2", 2, 2));
            gd.playerHands.get(player2Id).add(createCreature("Bear3", 2, 2));

            drs.resolveDealDamageIfFewCardsInHand(gd, entry, effect);

            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("does nothing")));
            verifyNoInteractions(triggerCollectionService);
        }
    }

    // =========================================================================
    // DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDealDamageToTargetCreatureEqualToControlledSubtypeCount")
    class ResolveDealDamageToTargetCreatureEqualToControlledSubtypeCount {

        @Test
        @DisplayName("Deals damage equal to controlled subtype count — creature survives")
        void dealsDamageEqualToSubtypeCount() {
            Card spittingCard = createCard("Spitting Earth");
            Permanent angel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
            StackEntry entry = createEntry(spittingCard, player1Id, angel.getId());
            DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect effect =
                    new DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype.MOUNTAIN, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(angel, 4);
            stubNoKeywordsOnSource(entry);
            when(gameQueryService.countControlledSubtypePermanents(gd, player1Id, CardSubtype.MOUNTAIN)).thenReturn(3);
            when(gameQueryService.findPermanentById(gd, angel.getId())).thenReturn(angel);
            when(gameQueryService.isLethalDamage(3, 4, false)).thenReturn(false);

            drs.resolveDealDamageToTargetCreatureEqualToControlledSubtypeCount(gd, entry, effect);

            assertThat(angel.getMarkedDamage()).isEqualTo(3);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, angel, 3, player1Id);
        }

        @Test
        @DisplayName("Kills creature when damage equals toughness")
        void killsCreatureWhenDamageEqualsOrExceedsToughness() {
            Card spittingCard = createCard("Spitting Earth");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntry(spittingCard, player1Id, bears.getId());
            DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect effect =
                    new DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype.MOUNTAIN, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(true);
            when(gameQueryService.countControlledSubtypePermanents(gd, player1Id, CardSubtype.MOUNTAIN)).thenReturn(2);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);

            drs.resolveDealDamageToTargetCreatureEqualToControlledSubtypeCount(gd, entry, effect);

            assertThat(bears.getMarkedDamage()).isEqualTo(2);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
        }

        @Test
        @DisplayName("Deals 0 damage when controller has no subtypes")
        void dealsZeroDamageWithNoSubtypes() {
            Card spittingCard = createCard("Spitting Earth");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createEntry(spittingCard, player1Id, bears.getId());
            DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect effect =
                    new DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype.MOUNTAIN, false);

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubNoKeywordsOnSource(entry);
            when(gameQueryService.countControlledSubtypePermanents(gd, player1Id, CardSubtype.MOUNTAIN)).thenReturn(0);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.isLethalDamage(0, 2, false)).thenReturn(false);

            drs.resolveDealDamageToTargetCreatureEqualToControlledSubtypeCount(gd, entry, effect);

            assertThat(bears.getMarkedDamage()).isEqualTo(0);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            verifyNoInteractions(triggerCollectionService);
        }
    }

    // =========================================================================
    // DealOrderedDamageToAnyTargetsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDealOrderedDamageToAnyTargets")
    class ResolveDealOrderedDamageToAnyTargets {

        @Test
        @DisplayName("Deals ordered damage to two creature targets")
        void dealsOrderedDamageToTwoCreatures() {
            Card arcCard = createCard("Arc Trail");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            Permanent elves = addPermanent(player2Id, createCreature("Llanowar Elves", 1, 1));
            StackEntry entry = createMultiTargetEntry(arcCard, player1Id, List.of(bears.getId(), elves.getId()));
            DealOrderedDamageToAnyTargetsEffect effect = new DealOrderedDamageToAnyTargetsEffect(List.of(2, 1));

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            stubCreatureDamageCore(elves, 1);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.findPermanentById(gd, elves.getId())).thenReturn(elves);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(true);
            when(gameQueryService.hasKeyword(eq(gd), any(Permanent.class), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);
            when(graveyardService.tryRegenerate(eq(gd), any(Permanent.class))).thenReturn(false);

            drs.resolveDealOrderedDamageToAnyTargets(gd, entry, effect);

            assertThat(bears.getMarkedDamage()).isEqualTo(2);
            assertThat(elves.getMarkedDamage()).isEqualTo(1);
            verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
            verify(permanentRemovalService).removePermanentToGraveyard(gd, elves);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, elves, 1, player1Id);
        }

        @Test
        @DisplayName("Deals damage to a creature and a player")
        void dealsDamageToCreatureAndPlayer() {
            Card arcCard = createCard("Arc Trail");
            Permanent bears = addPermanent(player2Id, createCreature("Grizzly Bears", 2, 2));
            StackEntry entry = createMultiTargetEntry(arcCard, player1Id, List.of(bears.getId(), player2Id));
            DealOrderedDamageToAnyTargetsEffect effect = new DealOrderedDamageToAnyTargetsEffect(List.of(2, 1));

            stubDamagePreventable();
            stubDamageFromSourceNotPrevented();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(bears, 2);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            stubNoKeywordsOnSource(entry);
            stubLethalDamage(true);
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, bears)).thenReturn(false);
            stubPlayerDamageCore(player2Id);

            drs.resolveDealOrderedDamageToAnyTargets(gd, entry, effect);

            assertThat(bears.getMarkedDamage()).isEqualTo(2);
            assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(19);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, bears, 2, player1Id);
            verify(triggerCollectionService).checkLifeLossTriggers(gd, player2Id, 1);
            verify(triggerCollectionService).checkDamageDealtToControllerTriggers(gd, player2Id, null, false);
            verify(triggerCollectionService).checkNoncombatDamageToOpponentTriggers(gd, player2Id);
        }
    }

    // =========================================================================
    // FirstTargetDealsPowerDamageToSecondTargetEffect (bite mechanic)
    // =========================================================================

    @Nested
    @DisplayName("resolveBite")
    class ResolveBite {

        @Test
        @DisplayName("Source creature deals its power as damage to target — target survives")
        void sourceDealsItsPowerAsDamageToTarget() {
            Card wingCard = createCard("Wing Puncture");
            wingCard.setColor(CardColor.GREEN);
            Permanent bears = addPermanent(player1Id, createCreature("Grizzly Bears", 2, 2));
            Permanent angel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
            StackEntry entry = createMultiTargetEntry(wingCard, player1Id, List.of(bears.getId(), angel.getId()));

            stubDamagePreventable();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(angel, 4);
            stubCreatureSourceRedirects();
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.findPermanentById(gd, angel.getId())).thenReturn(angel);
            when(gameQueryService.getPowerBasedDamage(gd, bears)).thenReturn(2);
            when(gameQueryService.isPreventedFromDealingDamage(gd, bears)).thenReturn(false);
            when(gameQueryService.hasProtectionFromSource(eq(gd), eq(angel), any(Permanent.class))).thenReturn(false);
            // Stub controller for the biting creature (used for trigger sourceControllerId)
            when(gameQueryService.findPermanentController(eq(gd), eq(bears.getId()))).thenReturn(player1Id);
            stubNoKeywordsOnSourceWithDamageSource(entry, bears);
            when(gameQueryService.isLethalDamage(2, 4, false)).thenReturn(false);

            drs.resolveBite(gd, entry);

            assertThat(angel.getMarkedDamage()).isEqualTo(2);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, angel, 2, player1Id);
        }

        @Test
        @DisplayName("Kills target when source power >= target toughness")
        void killsTargetWhenPowerIsLethal() {
            Card wingCard = createCard("Wing Puncture");
            wingCard.setColor(CardColor.GREEN);
            Permanent myAngel = addPermanent(player1Id, createCreature("Serra Angel", 4, 4));
            Permanent theirAngel = addPermanent(player2Id, createCreature("Serra Angel", 4, 4));
            StackEntry entry = createMultiTargetEntry(wingCard, player1Id, List.of(myAngel.getId(), theirAngel.getId()));

            stubDamagePreventable();
            stubNoDamageMultiplier();
            stubCreatureDamageCore(theirAngel, 4);
            stubCreatureSourceRedirects();
            when(gameQueryService.findPermanentById(gd, myAngel.getId())).thenReturn(myAngel);
            when(gameQueryService.findPermanentById(gd, theirAngel.getId())).thenReturn(theirAngel);
            when(gameQueryService.getPowerBasedDamage(gd, myAngel)).thenReturn(4);
            when(gameQueryService.isPreventedFromDealingDamage(gd, myAngel)).thenReturn(false);
            when(gameQueryService.hasProtectionFromSource(eq(gd), eq(theirAngel), any(Permanent.class))).thenReturn(false);
            // Stub controller for the biting creature (used for trigger sourceControllerId)
            when(gameQueryService.findPermanentController(eq(gd), eq(myAngel.getId()))).thenReturn(player1Id);
            stubNoKeywordsOnSourceWithDamageSource(entry, myAngel);
            when(gameQueryService.isLethalDamage(4, 4, false)).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, theirAngel, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, theirAngel)).thenReturn(false);

            drs.resolveBite(gd, entry);

            assertThat(theirAngel.getMarkedDamage()).isEqualTo(4);
            verify(triggerCollectionService).checkDealtDamageToCreatureTriggers(gd, theirAngel, 4, player1Id);
        }
    }

    // ===== resolveBoostColorSourceDamageThisTurn =====

    @Nested
    @DisplayName("resolveBoostColorSourceDamageThisTurn")
    class ResolveBoostColorSourceDamageThisTurn {

        @Test
        @DisplayName("sets color source damage bonus for controller")
        void setsBonusForController() {
            Card card = createCard("The Flame of Keld");
            BoostColorSourceDamageThisTurnEffect effect = new BoostColorSourceDamageThisTurnEffect(CardColor.RED, 2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                    "The Flame of Keld chapter III", new ArrayList<>(List.of(effect)), null);

            drs.resolveBoostColorSourceDamageThisTurn(gd, entry, effect);

            assertThat(gd.colorSourceDamageBonusThisTurn.get(player1Id).get(CardColor.RED)).isEqualTo(2);
        }

        @Test
        @DisplayName("stacks additively with existing bonus")
        void stacksAdditively() {
            gd.colorSourceDamageBonusThisTurn
                    .computeIfAbsent(player1Id, k -> new java.util.concurrent.ConcurrentHashMap<>())
                    .put(CardColor.RED, 2);

            Card card = createCard("Second Flame");
            BoostColorSourceDamageThisTurnEffect effect = new BoostColorSourceDamageThisTurnEffect(CardColor.RED, 3);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                    "Second Flame", new ArrayList<>(List.of(effect)), null);

            drs.resolveBoostColorSourceDamageThisTurn(gd, entry, effect);

            assertThat(gd.colorSourceDamageBonusThisTurn.get(player1Id).get(CardColor.RED)).isEqualTo(5);
        }

        @Test
        @DisplayName("does not affect other player's bonus")
        void doesNotAffectOtherPlayer() {
            Card card = createCard("The Flame of Keld");
            BoostColorSourceDamageThisTurnEffect effect = new BoostColorSourceDamageThisTurnEffect(CardColor.RED, 2);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                    "The Flame of Keld chapter III", new ArrayList<>(List.of(effect)), null);

            drs.resolveBoostColorSourceDamageThisTurn(gd, entry, effect);

            assertThat(gd.colorSourceDamageBonusThisTurn.getOrDefault(player2Id, java.util.Map.of()))
                    .doesNotContainKey(CardColor.RED);
        }
    }
}
