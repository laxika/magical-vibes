package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CombatDamagePhase1State;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CombatDamageServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private DamagePreventionService damagePreventionService;
    @Mock private GraveyardService graveyardService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private PlayerInputService playerInputService;
    @Mock private SessionManager sessionManager;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private LifeResolutionService lifeResolutionService;
    @Mock private CombatAttackService combatAttackService;
    @Mock private CombatTriggerService combatTriggerService;

    @InjectMocks
    private CombatDamageService combatDamageService;

    private GameData gameData;
    private UUID player1Id;
    private UUID player2Id;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        UUID gameId = UUID.randomUUID();
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        player1 = new Player(player1Id, "Player1");
        player2 = new Player(player2Id, "Player2");

        gameData = new GameData(gameId, "test-game", player1Id, "Player1");
        gameData.playerIds.add(player1Id);
        gameData.playerIds.add(player2Id);
        gameData.orderedPlayerIds.add(player1Id);
        gameData.orderedPlayerIds.add(player2Id);
        gameData.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerLifeTotals.put(player1Id, 20);
        gameData.playerLifeTotals.put(player2Id, 20);
        gameData.activePlayerId = player1Id;
        gameData.playerIdToName.put(player1Id, "Player1");
        gameData.playerIdToName.put(player2Id, "Player2");
    }

    // ===== Stub helpers =====

    /**
     * Core stubs for combat setup: opponent lookup, attacking creature indices,
     * keyword delegation, effective stats, and prevented-from-dealing-damage check.
     */
    private void stubCombatSetup() {
        when(gameQueryService.getOpponentId(gameData, player1Id)).thenReturn(player2Id);
        when(combatAttackService.getAttackingCreatureIndices(gameData, player1Id))
                .thenAnswer(inv -> {
                    List<Permanent> bf = gameData.playerBattlefields.get(player1Id);
                    List<Integer> indices = new ArrayList<>();
                    for (int i = 0; i < bf.size(); i++) {
                        if (bf.get(i).isAttacking()) indices.add(i);
                    }
                    return indices;
                });
        when(gameQueryService.hasKeyword(eq(gameData), any(Permanent.class), any(Keyword.class)))
                .thenAnswer(inv -> {
                    Permanent perm = inv.getArgument(1);
                    Keyword kw = inv.getArgument(2);
                    return perm.getCard().getKeywords().contains(kw);
                });
        when(gameQueryService.getEffectiveCombatDamage(eq(gameData), any(Permanent.class)))
                .thenAnswer(inv -> ((Permanent) inv.getArgument(1)).getCard().getPower());
        when(gameQueryService.getEffectiveToughness(eq(gameData), any(Permanent.class)))
                .thenAnswer(inv -> {
                    Permanent perm = inv.getArgument(1);
                    return perm.getCard().getToughness() - perm.getMinusOneMinusOneCounters();
                });
        when(gameQueryService.isPreventedFromDealingDamage(eq(gameData), any(Permanent.class), anyBoolean()))
                .thenReturn(false);
    }

    /**
     * Stubs for full damage resolution: lethality, multipliers, prevention shields,
     * controller lookups, redirect, and win condition. Requires stubCombatSetup().
     */
    private void stubDamageResolution() {
        when(gameQueryService.isLethalDamage(anyInt(), anyInt(), anyBoolean()))
                .thenAnswer(inv -> {
                    int damage = inv.getArgument(0);
                    int toughness = inv.getArgument(1);
                    boolean deathtouch = inv.getArgument(2);
                    return damage >= toughness || (deathtouch && damage >= 1);
                });
        when(gameQueryService.applyCombatDamageMultiplier(eq(gameData), anyInt(), any(), any()))
                .thenAnswer(inv -> (int) inv.getArgument(1));
        when(damagePreventionService.applyCreaturePreventionShield(
                eq(gameData), any(Permanent.class), anyInt(), anyBoolean()))
                .thenAnswer(inv -> (int) inv.getArgument(2));
        lenient().when(damagePreventionService.applyTargetSourcePreventionShield(
                eq(gameData), any(UUID.class), any(UUID.class), anyInt()))
                .thenAnswer(inv -> (int) inv.getArgument(3));
        when(gameQueryService.findPermanentController(eq(gameData), any(UUID.class)))
                .thenAnswer(inv -> {
                    UUID permId = inv.getArgument(1);
                    for (var entry : gameData.playerBattlefields.entrySet()) {
                        for (Permanent p : entry.getValue()) {
                            if (p.getId().equals(permId)) return entry.getKey();
                        }
                    }
                    return null;
                });
        when(damagePreventionService.applySourceRedirectShields(
                eq(gameData), any(UUID.class), any(UUID.class), anyInt()))
                .thenAnswer(inv -> (int) inv.getArgument(3));
        when(damagePreventionService.applyPlayerPreventionShield(
                eq(gameData), any(UUID.class), anyInt()))
                .thenAnswer(inv -> (int) inv.getArgument(2));
        when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(
                eq(gameData), any(UUID.class), anyInt(), anyString(), anyBoolean()))
                .thenAnswer(inv -> (int) inv.getArgument(2));
        when(gameOutcomeService.checkWinCondition(gameData)).thenReturn(false);
    }

    /** Stubs for blocked combat: isDamagePreventable and findPermanentById for triggers. */
    private void stubBlockedCombat() {
        when(gameQueryService.isDamagePreventable(gameData)).thenReturn(false);
        when(gameQueryService.findPermanentById(eq(gameData), any(UUID.class)))
                .thenAnswer(inv -> {
                    UUID permId = inv.getArgument(1);
                    for (var entry : gameData.playerBattlefields.entrySet()) {
                        for (Permanent p : entry.getValue()) {
                            if (p.getId().equals(permId)) return p;
                        }
                    }
                    return null;
                });
    }

    /** Stubs for unblocked regular (non-infect) damage reaching the player. */
    private void stubRegularPlayerDamage() {
        when(damagePreventionService.isSourceDamagePreventedForPlayer(
                eq(gameData), any(UUID.class), any(UUID.class))).thenReturn(false);
        when(damagePreventionService.applyColorDamagePreventionForPlayer(
                eq(gameData), any(UUID.class), any())).thenReturn(false);
        when(damagePreventionService.applyOpponentSourceDamageReduction(
                eq(gameData), any(UUID.class), any(), anyInt()))
                .thenAnswer(inv -> (int) inv.getArgument(3));
        lenient().when(damagePreventionService.applyTargetSourcePreventionShield(
                eq(gameData), any(UUID.class), any(UUID.class), anyInt()))
                .thenAnswer(inv -> (int) inv.getArgument(3));
        when(gameQueryService.shouldDamageBeDealtAsInfect(eq(gameData), any(UUID.class)))
                .thenReturn(false);
        when(gameQueryService.canPlayerLifeChange(eq(gameData), any(UUID.class))).thenReturn(true);
    }

    /** Stubs for infect damage to creatures (-1/-1 counters). */
    private void stubInfectOnCreature() {
        when(gameQueryService.cantHaveCounters(eq(gameData), any(Permanent.class))).thenReturn(false);
        when(gameQueryService.cantHaveMinusOneMinusOneCounters(eq(gameData), any(Permanent.class)))
                .thenReturn(false);
    }

    /** Stubs for infect damage to player (poison counters via accumulatePlayerDamage path). */
    private void stubInfectToPlayer() {
        when(damagePreventionService.isSourceDamagePreventedForPlayer(
                eq(gameData), any(UUID.class), any(UUID.class))).thenReturn(false);
        when(damagePreventionService.applyColorDamagePreventionForPlayer(
                eq(gameData), any(UUID.class), any())).thenReturn(false);
        when(damagePreventionService.applyOpponentSourceDamageReduction(
                eq(gameData), any(UUID.class), any(), anyInt()))
                .thenAnswer(inv -> (int) inv.getArgument(3));
        lenient().when(damagePreventionService.applyTargetSourcePreventionShield(
                eq(gameData), any(UUID.class), any(UUID.class), anyInt()))
                .thenAnswer(inv -> (int) inv.getArgument(3));
        when(gameQueryService.canPlayerGetPoisonCounters(eq(gameData), any(UUID.class)))
                .thenReturn(true);
    }

    // ===== Creature helpers =====

    private Card createCard(String name, int power, int toughness, Keyword... keywords) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        if (keywords.length > 0) {
            card.setKeywords(Set.of(keywords));
        }
        return card;
    }

    private Permanent addAttacker(String name, int power, int toughness, Keyword... keywords) {
        Card card = createCard(name, power, toughness, keywords);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gameData.playerBattlefields.get(player1Id).add(perm);
        return perm;
    }

    private Permanent addBlocker(String name, int power, int toughness, int attackerIndex,
                                  Keyword... keywords) {
        Card card = createCard(name, power, toughness, keywords);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setBlocking(true);
        perm.addBlockingTarget(attackerIndex);
        gameData.playerBattlefields.get(player2Id).add(perm);
        return perm;
    }

    private CombatDamagePhase1State emptyPhase1State(Map<Integer, List<Integer>> blockerMap) {
        return new CombatDamagePhase1State(
                Set.of(), Set.of(),
                Map.of(), Map.of(),
                Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                0, 0, Map.of(),
                blockerMap, false,
                Set.of(), Set.of()
        );
    }

    // ===== Unblocked Damage =====

    @Nested
    @DisplayName("Unblocked Damage")
    class UnblockedDamageTest {

        @BeforeEach
        void setUpStubs() {
            stubCombatSetup();
            stubDamageResolution();
            stubRegularPlayerDamage();
        }

        @Test
        @DisplayName("Unblocked attacker deals damage to defending player")
        void unblockedAttackerDealsDamage() {
            addAttacker("Bear", 2, 2);

            CombatResult result = combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.playerLifeTotals.get(player2Id)).isEqualTo(18);
            assertThat(result).isEqualTo(CombatResult.ADVANCE_AND_AUTO_PASS);
        }

        @Test
        @DisplayName("Multiple unblocked attackers deal combined damage")
        void multipleUnblockedAttackersDealCombinedDamage() {
            addAttacker("Bear", 2, 2);
            addAttacker("Elf", 1, 1);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.playerLifeTotals.get(player2Id)).isEqualTo(17);
        }
    }

    // ===== Blocked Combat =====

    @Nested
    @DisplayName("Blocked Combat")
    class BlockedCombatTest {

        @BeforeEach
        void setUpStubs() {
            stubCombatSetup();
            stubDamageResolution();
            stubBlockedCombat();
        }

        @Test
        @DisplayName("Equal creatures trade in combat")
        void equalCreaturesTrade() {
            Permanent attacker = addAttacker("Bear", 2, 2);
            Permanent blocker = addBlocker("Bear", 2, 2, 0);

            combatDamageService.resolveCombatDamage(gameData);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, attacker);
            verify(permanentRemovalService).removePermanentToGraveyard(gameData, blocker);
            assertThat(gameData.playerLifeTotals.get(player2Id)).isEqualTo(20);
        }

        @Test
        @DisplayName("Larger blocker survives and kills smaller attacker")
        void largerBlockerSurvives() {
            Permanent attacker = addAttacker("Bear", 2, 2);
            Permanent blocker = addBlocker("Spider", 2, 4, 0);

            combatDamageService.resolveCombatDamage(gameData);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, attacker);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(gameData, blocker);
        }
    }

    // ===== Mixed Combat =====

    @Nested
    @DisplayName("Mixed Combat Scenarios")
    class MixedCombatTest {

        @BeforeEach
        void setUpStubs() {
            stubCombatSetup();
            stubDamageResolution();
            stubBlockedCombat();
            stubRegularPlayerDamage();
        }

        @Test
        @DisplayName("One blocked attacker trades, one unblocked attacker deals player damage")
        void mixedBlockedAndUnblockedAttackers() {
            Permanent atk1 = addAttacker("Bear1", 2, 2);
            addAttacker("Bear2", 2, 2);
            Permanent blocker = addBlocker("Bear3", 2, 2, 0);

            combatDamageService.resolveCombatDamage(gameData);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, atk1);
            verify(permanentRemovalService).removePermanentToGraveyard(gameData, blocker);
            assertThat(gameData.playerLifeTotals.get(player2Id)).isEqualTo(18);
        }

        @Test
        @DisplayName("Three attackers: one blocked trades, two unblocked deal full damage")
        void threeAttackersTwoUnblocked() {
            addAttacker("Bear1", 2, 2);
            addAttacker("Elf", 1, 1);
            addAttacker("Bear2", 2, 2);
            addBlocker("Bear3", 2, 2, 0);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.playerLifeTotals.get(player2Id)).isEqualTo(17);
        }
    }

    // ===== First Strike =====

    @Nested
    @DisplayName("First Strike Phase Interactions")
    class FirstStrikePhaseTest {

        @BeforeEach
        void setUpStubs() {
            stubCombatSetup();
            stubDamageResolution();
            stubBlockedCombat();
        }

        @Test
        @DisplayName("Blocker with first strike kills attacker before it can deal damage")
        void firstStrikeBlockerKillsAttackerBeforeDamage() {
            Permanent attacker = addAttacker("Bear", 2, 2);
            Permanent blocker = addBlocker("Knight", 2, 1, 0, Keyword.FIRST_STRIKE);

            combatDamageService.resolveCombatDamage(gameData);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, attacker);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(gameData, blocker);
        }

        @Test
        @DisplayName("First strike attacker that doesn't kill blocker still takes damage in regular phase")
        void firstStrikeAttackerTakesDamageInRegularPhase() {
            Permanent attacker = addAttacker("Knight", 2, 1, Keyword.FIRST_STRIKE);
            Permanent blocker = addBlocker("Spider", 2, 4, 0);

            combatDamageService.resolveCombatDamage(gameData);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, attacker);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(gameData, blocker);
        }

        @Test
        @DisplayName("First strike attacker kills blocker before it can deal damage")
        void firstStrikeAttackerKillsBlockerBeforeDamage() {
            Permanent attacker = addAttacker("Knight", 2, 2, Keyword.FIRST_STRIKE);
            Permanent blocker = addBlocker("Bear", 2, 2, 0);

            combatDamageService.resolveCombatDamage(gameData);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, blocker);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(gameData, attacker);
        }
    }

    // ===== Double Strike =====

    @Nested
    @DisplayName("Double Strike")
    class DoubleStrikeTest {

        @Test
        @DisplayName("Double strike creature deals damage in both first-strike and regular phases")
        void doubleStrikeDealsDamageInBothPhases() {
            stubCombatSetup();
            stubDamageResolution();
            stubRegularPlayerDamage();

            addAttacker("Knight", 2, 2, Keyword.DOUBLE_STRIKE);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.playerLifeTotals.get(player2Id)).isEqualTo(16);
        }

        @Test
        @DisplayName("Double strike vs first strike: both deal damage in first-strike phase")
        void doubleStrikeVsFirstStrike() {
            stubCombatSetup();
            stubDamageResolution();
            stubBlockedCombat();

            Permanent attacker = addAttacker("DS Knight", 2, 1, Keyword.DOUBLE_STRIKE);
            Permanent blocker = addBlocker("FS Knight", 2, 1, 0, Keyword.FIRST_STRIKE);

            combatDamageService.resolveCombatDamage(gameData);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, attacker);
            verify(permanentRemovalService).removePermanentToGraveyard(gameData, blocker);
        }
    }

    // ===== Lifelink =====

    @Nested
    @DisplayName("Lifelink")
    class LifelinkTest {

        @Test
        @DisplayName("Lifelink creature gains life equal to combat damage dealt to player")
        void lifelinkGainsLifeOnPlayerDamage() {
            stubCombatSetup();
            stubDamageResolution();
            stubRegularPlayerDamage();

            addAttacker("Bear", 2, 2, Keyword.LIFELINK);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.playerLifeTotals.get(player2Id)).isEqualTo(18);
            verify(lifeResolutionService).applyGainLife(eq(gameData), eq(player1Id), eq(2),
                    eq("lifelink"));
        }

        @Test
        @DisplayName("Lifelink creature gains life equal to combat damage dealt to blocker")
        void lifelinkGainsLifeOnCreatureDamage() {
            stubCombatSetup();
            stubDamageResolution();
            stubBlockedCombat();

            addAttacker("Bear", 2, 2, Keyword.LIFELINK);
            addBlocker("Bear", 2, 2, 0);

            combatDamageService.resolveCombatDamage(gameData);

            verify(lifeResolutionService).applyGainLife(eq(gameData), eq(player1Id), eq(2),
                    eq("lifelink"));
        }

        @Test
        @DisplayName("Lifelink + double strike gains life from both damage phases")
        void lifelinkDoubleStrikeGainsTwice() {
            stubCombatSetup();
            stubDamageResolution();
            stubRegularPlayerDamage();

            addAttacker("Bear", 2, 2, Keyword.LIFELINK, Keyword.DOUBLE_STRIKE);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.playerLifeTotals.get(player2Id)).isEqualTo(16);
            verify(lifeResolutionService).applyGainLife(eq(gameData), eq(player1Id), eq(4),
                    eq("lifelink"));
        }

        @Test
        @DisplayName("Lifelink on blocker gains life for its controller")
        void lifelinkBlockerGainsLife() {
            stubCombatSetup();
            stubDamageResolution();
            stubBlockedCombat();

            addAttacker("Bear", 2, 2);
            addBlocker("Bear", 2, 2, 0, Keyword.LIFELINK);

            combatDamageService.resolveCombatDamage(gameData);

            verify(lifeResolutionService).applyGainLife(eq(gameData), eq(player2Id), eq(2),
                    eq("lifelink"));
        }
    }

    // ===== Marked Damage =====

    @Nested
    @DisplayName("Marked Damage")
    class MarkedDamageTest {

        @BeforeEach
        void setUpStubs() {
            stubCombatSetup();
            stubDamageResolution();
            stubBlockedCombat();
        }

        @Test
        @DisplayName("Surviving creature has marked damage after combat")
        void survivingCreatureHasMarkedDamage() {
            addAttacker("Bear", 2, 2);
            Permanent blocker = addBlocker("Spider", 2, 4, 0);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        }

        @Test
        @DisplayName("Combat damage tracks both attacker and blocker in permanentsDealtDamageThisTurn")
        void combatDamageTracksPermanentsDealtDamageThisTurn() {
            Permanent attacker = addAttacker("Bear", 2, 2);
            Permanent blocker = addBlocker("Spider", 2, 4, 0);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.permanentsDealtDamageThisTurn).contains(attacker.getId());
            assertThat(gameData.permanentsDealtDamageThisTurn).contains(blocker.getId());
        }

        @Test
        @DisplayName("Dead creature has marked damage equal to attacker power before removal")
        void deadCreatureHasMarkedDamage() {
            Permanent attacker = addAttacker("Bear", 2, 2);
            Permanent blocker = addBlocker("Bear", 2, 2, 0);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(attacker.getMarkedDamage()).isEqualTo(2);
            assertThat(blocker.getMarkedDamage()).isEqualTo(2);
            verify(permanentRemovalService).removePermanentToGraveyard(gameData, attacker);
            verify(permanentRemovalService).removePermanentToGraveyard(gameData, blocker);
        }
    }

    // ===== Indestructible =====

    @Nested
    @DisplayName("Indestructible in Combat")
    class IndestructibleTest {

        @BeforeEach
        void setUpStubs() {
            stubCombatSetup();
            stubDamageResolution();
            stubBlockedCombat();
        }

        @Test
        @DisplayName("Indestructible creature survives lethal combat damage")
        void indestructibleSurvivesLethalDamage() {
            Permanent attacker = addAttacker("Bear", 2, 2);
            Permanent blocker = addBlocker("Bear", 2, 2, 0, Keyword.INDESTRUCTIBLE);

            combatDamageService.resolveCombatDamage(gameData);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, attacker);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(gameData, blocker);
        }

        @Test
        @DisplayName("Indestructible creature still accumulates marked damage")
        void indestructibleStillAccumulatesMarkedDamage() {
            addAttacker("Bear", 2, 2);
            Permanent blocker = addBlocker("Bear", 2, 2, 0, Keyword.INDESTRUCTIBLE);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        }
    }

    // ===== Infect =====

    @Nested
    @DisplayName("Infect Combat Interactions")
    class InfectCombatTest {

        @Test
        @DisplayName("Infect damage to creature applies -1/-1 counters, not marked damage")
        void infectDamageAppliesCountersNotMarkedDamage() {
            stubCombatSetup();
            stubDamageResolution();
            stubBlockedCombat();
            stubInfectOnCreature();

            addAttacker("Mamba", 1, 1, Keyword.INFECT);
            Permanent blocker = addBlocker("Spider", 2, 4, 0);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(blocker.getMinusOneMinusOneCounters()).isEqualTo(1);
            assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        }

        @Test
        @DisplayName("Infect damage to player gives poison counters instead of life loss")
        void infectDamageGivesPoisonCounters() {
            stubCombatSetup();
            stubDamageResolution();
            stubInfectToPlayer();

            addAttacker("Mamba", 1, 1, Keyword.INFECT);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.playerLifeTotals.get(player2Id)).isEqualTo(20);
            assertThat(gameData.playerPoisonCounters.getOrDefault(player2Id, 0)).isEqualTo(1);
        }
    }

    // ===== Prevent All Combat Damage =====

    @Nested
    @DisplayName("Prevent All Combat Damage")
    class PreventAllCombatDamageTest {

        @Test
        @DisplayName("Prevent all combat damage returns ADVANCE_AND_AUTO_PASS immediately")
        void preventAllCombatDamageAdvancesImmediately() {
            addAttacker("Bear", 2, 2);
            addBlocker("Elf", 1, 1, 0);
            gameData.preventAllCombatDamage = true;

            CombatResult result = combatDamageService.resolveCombatDamage(gameData);

            assertThat(result).isEqualTo(CombatResult.ADVANCE_AND_AUTO_PASS);
            assertThat(gameData.playerLifeTotals.get(player2Id)).isEqualTo(20);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(eq(gameData), any());
        }
    }

    // ===== Orphaned Blocking State =====

    @Nested
    @DisplayName("Orphaned Blocking State Cleanup")
    class OrphanedBlockingStateTest {

        @BeforeEach
        void setUpStubs() {
            stubCombatSetup();
            stubDamageResolution();
            stubBlockedCombat();
        }

        @Test
        @DisplayName("Surviving blocker whose blocked attacker died has blocking state cleared")
        void survivingBlockerClearedWhenAttackerDies() {
            Permanent attacker = addAttacker("Elf", 1, 1);
            Permanent blocker = addBlocker("Spider", 2, 4, 0);
            blocker.addBlockingTargetId(attacker.getId());

            combatDamageService.resolveCombatDamage(gameData);

            verify(permanentRemovalService).removePermanentToGraveyard(gameData, attacker);
            assertThat(blocker.isBlocking()).isFalse();
            assertThat(blocker.getBlockingTargets()).isEmpty();
        }
    }

    // ===== Damage Tracking =====

    @Nested
    @DisplayName("Combat Damage Tracking")
    class CombatDamageTrackingTest {

        @Test
        @DisplayName("Defending player is tracked in playersDealtDamageThisTurn after combat damage")
        void defenderTrackedInDamagedPlayers() {
            stubCombatSetup();
            stubDamageResolution();
            stubRegularPlayerDamage();

            addAttacker("Bear", 2, 2);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.playersDealtDamageThisTurn).contains(player2Id);
        }

        @Test
        @DisplayName("Defending player not tracked when all combat damage is prevented")
        void defenderNotTrackedWhenDamagePrevented() {
            addAttacker("Bear", 2, 2);
            gameData.preventAllCombatDamage = true;

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.playersDealtDamageThisTurn).doesNotContain(player2Id);
        }

        @Test
        @DisplayName("Infect damage to player tracks damage dealt")
        void infectDamageTracksPlayer() {
            stubCombatSetup();
            stubDamageResolution();
            stubInfectToPlayer();

            addAttacker("Mamba", 1, 1, Keyword.INFECT);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.playersDealtDamageThisTurn).contains(player2Id);
        }
    }

    // ===== Trample Validation =====

    @Nested
    @DisplayName("Trample Damage Assignment Validation")
    class TrampleValidationTest {

        @BeforeEach
        void setUpStubs() {
            stubCombatSetup();
        }

        @Test
        @DisplayName("Trample: must assign at least lethal damage to each blocker before trampling")
        void trampleMustAssignLethalToBlocker() {
            Permanent attacker = addAttacker("Avatar", 8, 8, Keyword.TRAMPLE);
            Permanent blocker = addBlocker("Bear", 2, 2, 0);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.interaction.awaitingInputType())
                    .isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

            assertThatThrownBy(() -> combatDamageService.handleCombatDamageAssigned(
                    gameData, player1, 0, Map.of(
                            blocker.getId(), 1,
                            player2Id, 7
                    )))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Trample");
        }

        @Test
        @DisplayName("Trample: cannot assign damage to player if not enough to kill all blockers")
        void trampleCannotOverflowWhenNotEnoughForLethal() {
            Permanent attacker = addAttacker("Bear", 2, 2, Keyword.TRAMPLE);
            Permanent blocker1 = addBlocker("Bear1", 2, 2, 0);
            Permanent blocker2 = addBlocker("Bear2", 2, 2, 0);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.interaction.awaitingInputType())
                    .isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

            assertThatThrownBy(() -> combatDamageService.handleCombatDamageAssigned(
                    gameData, player1, 0, Map.of(
                            blocker1.getId(), 1,
                            player2Id, 1
                    )))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Trample");
        }

        @Test
        @DisplayName("Trample with deathtouch: only 1 damage needed per blocker to be lethal")
        void trampleDeathtouchOnlyNeedsOnePerBlocker() {
            Permanent attacker = addAttacker("Avatar", 8, 8, Keyword.TRAMPLE, Keyword.DEATHTOUCH);
            Permanent blocker1 = addBlocker("Bear1", 2, 2, 0);
            Permanent blocker2 = addBlocker("Bear2", 2, 2, 0);

            combatDamageService.resolveCombatDamage(gameData);

            assertThat(gameData.interaction.awaitingInputType())
                    .isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

            combatDamageService.handleCombatDamageAssigned(gameData, player1, 0, Map.of(
                    blocker1.getId(), 1,
                    blocker2.getId(), 1,
                    player2Id, 6
            ));

            assertThat(gameData.combatDamagePlayerAssignments).containsKey(0);
        }
    }

    // ===== handleCombatDamageAssigned Validation =====

    @Nested
    @DisplayName("Damage Assignment Validation")
    class DamageAssignmentValidationTest {

        @Test
        @DisplayName("Rejects damage assignment when not in combat damage phase")
        void rejectsWhenNotInCombatDamagePhase() {
            assertThatThrownBy(() -> combatDamageService.handleCombatDamageAssigned(
                    gameData, player1, 0, Map.of()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not in combat damage assignment phase");
        }

        @Test
        @DisplayName("Rejects damage assignment from non-active player")
        void rejectsFromNonActivePlayer() {
            setupPendingAssignment();

            assertThatThrownBy(() -> combatDamageService.handleCombatDamageAssigned(
                    gameData, player2, 0, Map.of()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only the active player");
        }

        @Test
        @DisplayName("Rejects assignment with wrong total damage")
        void rejectsWrongTotalDamage() {
            Permanent blocker1 = setupPendingAssignment();
            Permanent blocker2 = gameData.playerBattlefields.get(player2Id).get(1);

            when(gameQueryService.getOpponentId(gameData, player1Id)).thenReturn(player2Id);
            when(gameQueryService.getEffectiveCombatDamage(eq(gameData), any(Permanent.class)))
                    .thenAnswer(inv -> ((Permanent) inv.getArgument(1)).getCard().getPower());

            assertThatThrownBy(() -> combatDamageService.handleCombatDamageAssigned(
                    gameData, player1, 0, Map.of(
                            blocker1.getId(), 2,
                            blocker2.getId(), 1
                    )))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Total assigned damage");
        }

        @Test
        @DisplayName("Rejects assignment to invalid target")
        void rejectsInvalidTarget() {
            setupPendingAssignment();

            when(gameQueryService.getOpponentId(gameData, player1Id)).thenReturn(player2Id);
            when(gameQueryService.getEffectiveCombatDamage(eq(gameData), any(Permanent.class)))
                    .thenAnswer(inv -> ((Permanent) inv.getArgument(1)).getCard().getPower());
            when(gameQueryService.hasKeyword(eq(gameData), any(Permanent.class), any(Keyword.class)))
                    .thenAnswer(inv -> ((Permanent) inv.getArgument(1)).getCard().getKeywords()
                            .contains(inv.getArgument(2)));

            UUID bogusTarget = UUID.randomUUID();
            assertThatThrownBy(() -> combatDamageService.handleCombatDamageAssigned(
                    gameData, player1, 0, Map.of(bogusTarget, 2)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid damage target");
        }

        @Test
        @DisplayName("Non-trample creature cannot assign damage to defending player")
        void nonTrampleCannotAssignToPlayer() {
            setupPendingAssignment();

            when(gameQueryService.getOpponentId(gameData, player1Id)).thenReturn(player2Id);
            when(gameQueryService.getEffectiveCombatDamage(eq(gameData), any(Permanent.class)))
                    .thenAnswer(inv -> ((Permanent) inv.getArgument(1)).getCard().getPower());
            when(gameQueryService.hasKeyword(eq(gameData), any(Permanent.class), any(Keyword.class)))
                    .thenAnswer(inv -> ((Permanent) inv.getArgument(1)).getCard().getKeywords()
                            .contains(inv.getArgument(2)));

            assertThatThrownBy(() -> combatDamageService.handleCombatDamageAssigned(
                    gameData, player1, 0, Map.of(player2Id, 2)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid damage target");
        }

        @Test
        @DisplayName("Rejects assignment for attacker index that is not pending")
        void rejectsNonPendingAttackerIndex() {
            setupPendingAssignment();

            assertThatThrownBy(() -> combatDamageService.handleCombatDamageAssigned(
                    gameData, player1, 5, Map.of()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not pending");
        }

        private Permanent setupPendingAssignment() {
            Permanent attacker = addAttacker("Bear", 2, 2);
            Permanent blocker1 = addBlocker("Elf1", 1, 1, 0);
            Permanent blocker2 = addBlocker("Elf2", 1, 1, 0);

            gameData.combatDamagePhase1Complete = true;
            gameData.combatDamagePendingIndices.add(0);
            gameData.combatDamagePhase1State = emptyPhase1State(Map.of(0, List.of(0, 1)));

            return blocker1;
        }
    }

    // ===== Combat Damage To Player Triggers =====

    @Nested
    @DisplayName("Combat Damage To Player Trigger Targeting")
    class CombatDamageToPlayerTriggerTest {

        @BeforeEach
        void setUpStubs() {
            stubCombatSetup();
            stubDamageResolution();
            stubRegularPlayerDamage();
        }

        private Permanent addAttackerWithEffect(String name, int power, int toughness,
                                                 EffectSlot slot, com.github.laxika.magicalvibes.model.effect.CardEffect effect) {
            Card card = createCard(name, power, toughness);
            card.addEffect(slot, effect);
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            perm.setAttacking(true);
            gameData.playerBattlefields.get(player1Id).add(perm);
            return perm;
        }

        @Test
        @DisplayName("TargetPlayerDiscardsEffect stack entry has defenderId as targetId")
        void targetPlayerDiscardsEffectSetsDefenderAsTarget() {
            addAttackerWithEffect("Animated Sword", 3, 3,
                    EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new TargetPlayerDiscardsEffect(1));

            combatDamageService.resolveCombatDamage(gameData);

            List<StackEntry> triggerEntries = gameData.stack.stream()
                    .filter(se -> se.getEffectsToResolve().stream().anyMatch(e -> e instanceof TargetPlayerDiscardsEffect))
                    .toList();
            assertThat(triggerEntries).hasSize(1);
            assertThat(triggerEntries.getFirst().getTargetId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("MillTargetPlayerEffect stack entry has defenderId as targetId")
        void millTargetPlayerEffectSetsDefenderAsTarget() {
            addAttackerWithEffect("Animated Sword", 3, 3,
                    EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MillTargetPlayerEffect(10));

            combatDamageService.resolveCombatDamage(gameData);

            List<StackEntry> triggerEntries = gameData.stack.stream()
                    .filter(se -> se.getEffectsToResolve().stream().anyMatch(e -> e instanceof MillTargetPlayerEffect))
                    .toList();
            assertThat(triggerEntries).hasSize(1);
            assertThat(triggerEntries.getFirst().getTargetId()).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("DealDamageToTargetPlayerByHandSizeEffect stack entry has defenderId as targetId")
        void dealDamageByHandSizeEffectSetsDefenderAsTarget() {
            addAttackerWithEffect("Animated Sword", 3, 3,
                    EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DealDamageToTargetPlayerByHandSizeEffect());

            combatDamageService.resolveCombatDamage(gameData);

            List<StackEntry> triggerEntries = gameData.stack.stream()
                    .filter(se -> se.getEffectsToResolve().stream().anyMatch(e -> e instanceof DealDamageToTargetPlayerByHandSizeEffect))
                    .toList();
            assertThat(triggerEntries).hasSize(1);
            assertThat(triggerEntries.getFirst().getTargetId()).isEqualTo(player2Id);
        }
    }
}
