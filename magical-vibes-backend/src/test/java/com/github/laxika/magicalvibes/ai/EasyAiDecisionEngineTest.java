package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongTargetCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EasyAiDecisionEngineTest {

    @Mock private MessageHandler messageHandler;
    @Mock private GameQueryService gameQueryService;
    @Mock private CombatAttackService combatAttackService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private com.github.laxika.magicalvibes.service.effect.TargetValidationService targetValidationService;
    @Mock private Connection selfConnection;

    private GameData gd;
    private Player aiPlayer;
    private GameRegistry gameRegistry;

    @BeforeEach
    void setUp() {
        UUID gameId = UUID.randomUUID();
        aiPlayer = new Player(UUID.randomUUID(), "AI");
        Player opponent = new Player(UUID.randomUUID(), "Opponent");

        gd = new GameData(gameId, "test", aiPlayer.getId(), "AI");
        gd.status = GameStatus.RUNNING;
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
        gd.activePlayerId = aiPlayer.getId();
        gd.orderedPlayerIds.add(aiPlayer.getId());
        gd.orderedPlayerIds.add(opponent.getId());
        gd.playerIdToName.put(aiPlayer.getId(), "AI");
        gd.playerIdToName.put(opponent.getId(), "Opponent");
        gd.playerHands.put(aiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(opponent.getId(), Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(aiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(opponent.getId(), Collections.synchronizedList(new ArrayList<>()));
        gd.playerManaPools.put(aiPlayer.getId(), new ManaPool());
        gd.playerManaPools.put(opponent.getId(), new ManaPool());
        gd.playerLifeTotals.put(aiPlayer.getId(), 20);
        gd.playerLifeTotals.put(opponent.getId(), 20);
        gd.playerDecks.put(aiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(opponent.getId(), Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(aiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(opponent.getId(), Collections.synchronizedList(new ArrayList<>()));

        gameRegistry = new GameRegistry();
        gameRegistry.register(gd);
    }

    private EasyAiDecisionEngine createEngine() {
        Mockito.lenient().when(gameBroadcastService.isSpellCastingAllowed(any(), any(), any())).thenReturn(true);
        EasyAiDecisionEngine engine = new EasyAiDecisionEngine(
                gd.id, aiPlayer, gameRegistry, messageHandler,
                gameQueryService, combatAttackService, gameBroadcastService,
                targetValidationService);
        engine.setSelfConnection(selfConnection);
        return engine;
    }

    // ===== Creature mana restriction =====

    @Test
    @DisplayName("Easy AI does not attempt to cast requiresCreatureMana card with only land mana")
    void doesNotCastCreatureManaCardWithLandMana() throws Exception {
        Card myrSuperion = new Card();
        myrSuperion.setName("Myr Superion");
        myrSuperion.setType(CardType.CREATURE);
        myrSuperion.setManaCost("{2}");
        myrSuperion.setPower(5);
        myrSuperion.setToughness(6);
        myrSuperion.setRequiresCreatureMana(true);
        gd.playerHands.get(aiPlayer.getId()).add(myrSuperion);

        // Only land mana available — no creature mana
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.COLORLESS, 2);

        createEngine().handleMessage("GAME_STATE", "");

        // Should NOT attempt to cast — creature mana requirement not met
        verify(messageHandler, never()).handlePlayCard(any(), any());
        verify(messageHandler).handlePassPriority(any(), any());
    }

    @Test
    @DisplayName("Easy AI casts requiresCreatureMana card when creature mana is available")
    void castsCreatureManaCardWithCreatureMana() throws Exception {
        Card myrSuperion = new Card();
        myrSuperion.setName("Myr Superion");
        myrSuperion.setType(CardType.CREATURE);
        myrSuperion.setManaCost("{2}");
        myrSuperion.setPower(5);
        myrSuperion.setToughness(6);
        myrSuperion.setRequiresCreatureMana(true);
        gd.playerHands.get(aiPlayer.getId()).add(myrSuperion);

        // Creature mana available
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.COLORLESS, 2);
        pool.addCreatureMana(ManaColor.COLORLESS, 2);

        createEngine().handleMessage("GAME_STATE", "");

        // Should attempt to cast — creature mana requirement met
        verify(messageHandler).handlePlayCard(any(), any());
    }

    // ===== Sacrifice cost restriction =====

    @Test
    @DisplayName("Easy AI does not attempt to cast spell with SacrificeArtifactCost when no artifact available")
    void doesNotCastSacrificeArtifactCostWithNoArtifact() throws Exception {
        Card sacrificeSpell = new Card();
        sacrificeSpell.setName("Test Artifact Sac");
        sacrificeSpell.setType(CardType.SORCERY);
        sacrificeSpell.setManaCost("{R}");
        sacrificeSpell.addEffect(EffectSlot.SPELL, new SacrificeArtifactCost());
        gd.playerHands.get(aiPlayer.getId()).add(sacrificeSpell);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);

        createEngine().handleMessage("GAME_STATE", "");

        // Should NOT attempt to cast — no artifact to sacrifice
        verify(messageHandler, never()).handlePlayCard(any(), any());
        verify(messageHandler).handlePassPriority(any(), any());
    }

    @Test
    @DisplayName("Easy AI does not attempt to cast spell with SacrificeCreatureCost when no creature available")
    void doesNotCastSacrificeCreatureCostWithNoCreature() throws Exception {
        Card sacrificeSpell = new Card();
        sacrificeSpell.setName("Test Creature Sac");
        sacrificeSpell.setType(CardType.SORCERY);
        sacrificeSpell.setManaCost("{R}");
        sacrificeSpell.addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        gd.playerHands.get(aiPlayer.getId()).add(sacrificeSpell);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);

        createEngine().handleMessage("GAME_STATE", "");

        // Should NOT attempt to cast — no creature to sacrifice
        verify(messageHandler, never()).handlePlayCard(any(), any());
        verify(messageHandler).handlePassPriority(any(), any());
    }

    // ===== Sacrifice cost passes sacrificePermanentId =====

    @Test
    @DisplayName("Easy AI passes sacrificePermanentId in PlayCardRequest for sacrifice-cost spell")
    void passesSacrificePermanentIdInPlayCardRequest() throws Exception {
        Card sacrificeSpell = new Card();
        sacrificeSpell.setName("Test Sac Spell");
        sacrificeSpell.setType(CardType.SORCERY);
        sacrificeSpell.setManaCost("{R}");
        sacrificeSpell.addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        gd.playerHands.get(aiPlayer.getId()).add(sacrificeSpell);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);

        // Add a creature to sacrifice
        Card creatureCard = new Card();
        creatureCard.setName("Sacrifice Fodder");
        creatureCard.setType(CardType.CREATURE);
        creatureCard.setPower(1);
        creatureCard.setToughness(1);
        Permanent creature = new Permanent(creatureCard);
        gd.playerBattlefields.get(aiPlayer.getId()).add(creature);

        when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
        Mockito.lenient().when(gameQueryService.getEffectivePower(gd, creature)).thenReturn(1);
        Mockito.lenient().when(gameQueryService.getEffectiveToughness(gd, creature)).thenReturn(1);

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any(), any());

        createEngine().handleMessage("GAME_STATE", "");

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(eq(selfConnection), captor.capture());

        PlayCardRequest request = captor.getValue();
        assertThat(request.sacrificePermanentId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Easy AI passes null sacrificePermanentId for spells without sacrifice cost")
    void passesNullSacrificePermanentIdForNormalSpell() throws Exception {
        Card creature = new Card();
        creature.setName("Test Bear");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{1}{G}");
        creature.setPower(2);
        creature.setToughness(2);
        gd.playerHands.get(aiPlayer.getId()).add(creature);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.GREEN, 1);
        pool.add(ManaColor.COLORLESS, 1);

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any(), any());

        createEngine().handleMessage("GAME_STATE", "");

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(eq(selfConnection), captor.capture());

        PlayCardRequest request = captor.getValue();
        assertThat(request.sacrificePermanentId()).isNull();
    }

    // ===== tryCastSpell silent failure recovery =====

    @Test
    @DisplayName("Easy AI passes priority when spell cast is silently rejected")
    void passesPriorityWhenSpellCastSilentlyRejected() throws Exception {
        Card creature = new Card();
        creature.setName("Test Bear");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{1}{G}");
        creature.setPower(2);
        creature.setToughness(2);
        gd.playerHands.get(aiPlayer.getId()).add(creature);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.GREEN, 1);
        pool.add(ManaColor.COLORLESS, 1);

        createEngine().handleMessage("GAME_STATE", "");

        verify(messageHandler).handlePlayCard(any(), any());
        verify(messageHandler).handlePassPriority(any(), any());
    }

    @Test
    @DisplayName("Easy AI does NOT pass priority when spell cast succeeds")
    void doesNotPassPriorityWhenSpellCastSucceeds() throws Exception {
        Card creature = new Card();
        creature.setName("Test Bear");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{1}{G}");
        creature.setPower(2);
        creature.setToughness(2);
        gd.playerHands.get(aiPlayer.getId()).add(creature);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.GREEN, 1);
        pool.add(ManaColor.COLORLESS, 1);

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any(), any());

        createEngine().handleMessage("GAME_STATE", "");

        verify(messageHandler).handlePlayCard(any(), any());
        verify(messageHandler, never()).handlePassPriority(any(), any());
    }

    // ===== Blocker eligibility uses canBlock =====

    @Test
    @DisplayName("Easy AI skips creatures that canBlock() returns false for when declaring blockers")
    void skipsCreaturesThatCannotBlock() throws Exception {
        UUID opponentId = gd.orderedPlayerIds.get(1);

        // Opponent has a 3/3 attacking creature
        Card attackerCard = new Card();
        attackerCard.setName("Opponent Bear");
        attackerCard.setType(CardType.CREATURE);
        attackerCard.setPower(3);
        attackerCard.setToughness(3);
        Permanent attacker = new Permanent(attackerCard);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(opponentId).add(attacker);

        // AI has a 4/4 creature that cannot block (e.g. has CantBlockEffect)
        Card cantBlockCard = new Card();
        cantBlockCard.setName("Restricted Creature");
        cantBlockCard.setType(CardType.CREATURE);
        cantBlockCard.setPower(4);
        cantBlockCard.setToughness(4);
        Permanent cantBlocker = new Permanent(cantBlockCard);
        gd.playerBattlefields.get(aiPlayer.getId()).add(cantBlocker);

        // gameQueryService.canBlock returns false for the restricted creature
        when(gameQueryService.canBlock(gd, cantBlocker)).thenReturn(false);

        createEngine().handleMessage("AVAILABLE_BLOCKERS", "");

        // Should declare no blockers since the only creature can't block
        ArgumentCaptor<DeclareBlockersRequest> captor = ArgumentCaptor.forClass(DeclareBlockersRequest.class);
        verify(messageHandler).handleDeclareBlockers(eq(selfConnection), captor.capture());
        assertThat(captor.getValue().blockerAssignments()).isEmpty();
    }

    // ===== Spell casting restrictions (cost modifiers, spell limits) =====

    @Test
    @DisplayName("Easy AI does not cast spell when cost modifier makes it unaffordable")
    void doesNotCastWhenCostModifierMakesUnaffordable() throws Exception {
        Card creature = new Card();
        creature.setName("Test Bear");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{1}{G}");
        creature.setPower(2);
        creature.setToughness(2);
        gd.playerHands.get(aiPlayer.getId()).add(creature);

        // Exactly enough mana for base cost {1}{G} = 2 total
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.GREEN, 1);
        pool.add(ManaColor.COLORLESS, 1);

        // Cost modifier adds 1 (e.g. opponent has Thalia) — now needs 3 total
        when(gameBroadcastService.getCastCostModifier(any(), any(), any())).thenReturn(1);

        createEngine().handleMessage("GAME_STATE", "");

        // Should NOT attempt to cast — can't afford with cost increase
        verify(messageHandler, never()).handlePlayCard(any(), any());
        verify(messageHandler).handlePassPriority(any(), any());
    }

    @Test
    @DisplayName("Easy AI casts spell when cost modifier is negative (cost reduction)")
    void castsSpellWithCostReduction() throws Exception {
        Card creature = new Card();
        creature.setName("Expensive Creature");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{3}{G}");
        creature.setPower(4);
        creature.setToughness(4);
        gd.playerHands.get(aiPlayer.getId()).add(creature);

        // Only 3 mana available — normally can't afford {3}{G} (4 total)
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.GREEN, 1);
        pool.add(ManaColor.COLORLESS, 2);

        // Cost reduction of 1 — now only needs 3 total
        when(gameBroadcastService.getCastCostModifier(any(), any(), any())).thenReturn(-1);

        createEngine().handleMessage("GAME_STATE", "");

        // Should attempt to cast — affordable with cost reduction
        verify(messageHandler).handlePlayCard(any(), any());
    }

    @Test
    @DisplayName("Easy AI does not cast spell when isSpellCastingAllowed returns false")
    void doesNotCastWhenSpellCastingNotAllowed() throws Exception {
        Card creature = new Card();
        creature.setName("Test Bear");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{1}{G}");
        creature.setPower(2);
        creature.setToughness(2);
        gd.playerHands.get(aiPlayer.getId()).add(creature);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.GREEN, 1);
        pool.add(ManaColor.COLORLESS, 1);

        // Spell casting not allowed (e.g. spell limit reached, type restricted, silenced)
        when(gameBroadcastService.isSpellCastingAllowed(any(), any(), any())).thenReturn(false);

        EasyAiDecisionEngine engine = new EasyAiDecisionEngine(
                gd.id, aiPlayer, gameRegistry, messageHandler,
                gameQueryService, combatAttackService, gameBroadcastService,
                targetValidationService);
        engine.setSelfConnection(selfConnection);
        engine.handleMessage("GAME_STATE", "");

        // Should NOT attempt to cast — spell casting restricted
        verify(messageHandler, never()).handlePlayCard(any(), any());
        verify(messageHandler).handlePassPriority(any(), any());
    }

    // ===== Divided damage spells =====

    @Test
    @DisplayName("Easy AI builds damage assignments for divided damage spell targeting single creature")
    void buildsDamageAssignmentsForSingleTarget() throws Exception {
        Card spell = new Card();
        spell.setName("Test Divided Damage");
        spell.setType(CardType.SORCERY);
        spell.setManaCost("{1}{R}");
        spell.target(null, 1, 3)
                .addEffect(EffectSlot.SPELL, new DealDividedDamageAmongTargetCreaturesEffect(3));
        gd.playerHands.get(aiPlayer.getId()).add(spell);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);
        pool.add(ManaColor.COLORLESS, 1);

        UUID opponentId = gd.orderedPlayerIds.get(1);
        Card creatureCard = new Card();
        creatureCard.setName("Opponent Creature");
        creatureCard.setType(CardType.CREATURE);
        creatureCard.setPower(2);
        creatureCard.setToughness(3);
        Permanent creature = new Permanent(creatureCard);
        gd.playerBattlefields.get(opponentId).add(creature);

        when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
        when(gameQueryService.getEffectiveToughness(gd, creature)).thenReturn(3);
        when(targetValidationService.checkEffectTargets(any(), any())).thenReturn(Optional.empty());

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any(), any());

        createEngine().handleMessage("GAME_STATE", "");

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(eq(selfConnection), captor.capture());

        PlayCardRequest request = captor.getValue();
        assertThat(request.damageAssignments()).isNotNull();
        assertThat(request.damageAssignments()).containsEntry(creature.getId(), 3);
    }

    @Test
    @DisplayName("Easy AI does not cast divided damage spell when no valid targets exist")
    void doesNotCastDividedDamageSpellWithNoValidTargets() throws Exception {
        Card spell = new Card();
        spell.setName("Test Divided Damage");
        spell.setType(CardType.SORCERY);
        spell.setManaCost("{1}{R}");
        spell.target(null, 1, 3)
                .addEffect(EffectSlot.SPELL, new DealDividedDamageAmongTargetCreaturesEffect(3));
        gd.playerHands.get(aiPlayer.getId()).add(spell);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);
        pool.add(ManaColor.COLORLESS, 1);

        // No creatures on opponent's battlefield

        createEngine().handleMessage("GAME_STATE", "");

        verify(messageHandler, never()).handlePlayCard(any(), any());
        verify(messageHandler).handlePassPriority(any(), any());
    }

    @Test
    @DisplayName("Easy AI distributes divided damage among multiple creatures to maximize kills")
    void distributesDividedDamageToMaximizeKills() throws Exception {
        Card spell = new Card();
        spell.setName("Test Divided Damage");
        spell.setType(CardType.SORCERY);
        spell.setManaCost("{1}{R}");
        spell.target(null, 1, 3)
                .addEffect(EffectSlot.SPELL, new DealDividedDamageAmongTargetCreaturesEffect(3));
        gd.playerHands.get(aiPlayer.getId()).add(spell);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);
        pool.add(ManaColor.COLORLESS, 1);

        UUID opponentId = gd.orderedPlayerIds.get(1);

        Card c1 = new Card();
        c1.setName("Small Creature");
        c1.setType(CardType.CREATURE);
        c1.setPower(1);
        c1.setToughness(1);
        Permanent creature1 = new Permanent(c1);

        Card c2 = new Card();
        c2.setName("Medium Creature");
        c2.setType(CardType.CREATURE);
        c2.setPower(2);
        c2.setToughness(2);
        Permanent creature2 = new Permanent(c2);

        gd.playerBattlefields.get(opponentId).add(creature1);
        gd.playerBattlefields.get(opponentId).add(creature2);

        when(gameQueryService.isCreature(gd, creature1)).thenReturn(true);
        when(gameQueryService.isCreature(gd, creature2)).thenReturn(true);
        when(gameQueryService.getEffectiveToughness(gd, creature1)).thenReturn(1);
        when(gameQueryService.getEffectiveToughness(gd, creature2)).thenReturn(2);
        when(targetValidationService.checkEffectTargets(any(), any())).thenReturn(Optional.empty());

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any(), any());

        createEngine().handleMessage("GAME_STATE", "");

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(eq(selfConnection), captor.capture());

        PlayCardRequest request = captor.getValue();
        assertThat(request.damageAssignments()).isNotNull();
        assertThat(request.damageAssignments()).hasSize(2);
        // Should assign 1 to the 1/1 and 2 to the 2/2 (kills both)
        assertThat(request.damageAssignments()).containsEntry(creature1.getId(), 1);
        assertThat(request.damageAssignments()).containsEntry(creature2.getId(), 2);
    }

    // ===== X-spell cost modifier handling =====

    @Test
    @DisplayName("Easy AI reduces X value when cost modifier is present")
    void reducesXValueWithCostModifier() throws Exception {
        // X-cost sorcery: {X}{B}{B}
        Card xSpell = new Card();
        xSpell.setName("Test X Spell");
        xSpell.setType(CardType.SORCERY);
        xSpell.setManaCost("{X}{B}{B}");
        gd.playerHands.get(aiPlayer.getId()).add(xSpell);

        // 4 black mana → without modifier maxX=2, with modifier +1 maxX=1
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.BLACK, 4);

        // Cost modifier +1 (e.g. Thalia on opponent's battlefield)
        when(gameBroadcastService.getCastCostModifier(any(), any(), any())).thenReturn(1);

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any(), any());

        createEngine().handleMessage("GAME_STATE", "");

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(eq(selfConnection), captor.capture());

        PlayCardRequest request = captor.getValue();
        // maxX should be 1 (4 total - 2 for BB - 1 for modifier = 1), so X must be 1
        assertThat(request.xValue()).isEqualTo(1);
    }

    @Test
    @DisplayName("Easy AI does not cast X-spell when cost modifier makes X=0")
    void doesNotCastXSpellWhenCostModifierMakesXZero() throws Exception {
        // X-cost sorcery: {X}{B}{B}
        Card xSpell = new Card();
        xSpell.setName("Test X Spell");
        xSpell.setType(CardType.SORCERY);
        xSpell.setManaCost("{X}{B}{B}");
        gd.playerHands.get(aiPlayer.getId()).add(xSpell);

        // 4 black mana → without modifier maxX=2, with modifier +2 maxX=0
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.BLACK, 4);

        // Cost modifier +2 — no X value is affordable
        when(gameBroadcastService.getCastCostModifier(any(), any(), any())).thenReturn(2);

        createEngine().handleMessage("GAME_STATE", "");

        // Should NOT attempt to cast — maxX is 0
        verify(messageHandler, never()).handlePlayCard(any(), any());
        verify(messageHandler).handlePassPriority(any(), any());
    }

    @Test
    @DisplayName("Easy AI casts X-spell at full X when no cost modifier is present")
    void castsXSpellAtFullXWithNoCostModifier() throws Exception {
        // X-cost sorcery: {X}{B}{B}
        Card xSpell = new Card();
        xSpell.setName("Test X Spell");
        xSpell.setType(CardType.SORCERY);
        xSpell.setManaCost("{X}{B}{B}");
        gd.playerHands.get(aiPlayer.getId()).add(xSpell);

        // 4 black mana → maxX=2
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.BLACK, 4);

        // No cost modifier
        when(gameBroadcastService.getCastCostModifier(any(), any(), any())).thenReturn(0);

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any(), any());

        createEngine().handleMessage("GAME_STATE", "");

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(eq(selfConnection), captor.capture());

        PlayCardRequest request = captor.getValue();
        // maxX should be 2, smartX clamps to target toughness but no target → full maxX
        assertThat(request.xValue()).isEqualTo(2);
    }

    // ===== Attack tax handling =====

    @Test
    @DisplayName("Easy AI caps attackers to affordable count when attack tax is present")
    void capsAttackersWhenAttackTaxPresent() throws Exception {
        gd.currentStep = TurnStep.DECLARE_ATTACKERS;
        gd.interaction.setAwaitingInput(com.github.laxika.magicalvibes.model.AwaitingInput.ATTACKER_DECLARATION);

        // 3 creatures on the AI battlefield
        for (int i = 0; i < 3; i++) {
            Permanent creature = new Permanent(new Card());
            creature.getCard().setName("Bear " + i);
            creature.getCard().setType(CardType.CREATURE);
            creature.getCard().setPower(2);
            creature.getCard().setToughness(2);
            creature.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(creature);
        }

        // AI has 2 mana in pool — tax is {1} per creature, so can afford at most 2
        gd.playerManaPools.get(aiPlayer.getId()).add(ManaColor.COLORLESS, 2);

        when(combatAttackService.getAttackableCreatureIndices(gd, aiPlayer.getId()))
                .thenReturn(List.of(0, 1, 2));
        when(combatAttackService.getMustAttackIndices(eq(gd), eq(aiPlayer.getId()), any()))
                .thenReturn(List.of());
        when(gameBroadcastService.getAttackPaymentPerCreature(gd, aiPlayer.getId()))
                .thenReturn(1);
        when(gameQueryService.getEffectivePower(eq(gd), any())).thenReturn(2);
        when(gameQueryService.getEffectiveToughness(eq(gd), any())).thenReturn(2);

        createEngine().handleMessage("AVAILABLE_ATTACKERS", "");

        ArgumentCaptor<DeclareAttackersRequest> captor = ArgumentCaptor.forClass(DeclareAttackersRequest.class);
        verify(messageHandler).handleDeclareAttackers(eq(selfConnection), captor.capture());

        assertThat(captor.getValue().attackerIndices()).hasSizeLessThanOrEqualTo(2);
    }

    // ===== ExileNCardsFromGraveyardCost (e.g. Skaab Ruinator) =====

    @Test
    @DisplayName("Easy AI passes exileGraveyardCardIndices in PlayCardRequest for ExileNCardsFromGraveyardCost")
    void passesExileGraveyardCardIndicesForExileNCost() throws Exception {
        Card skaab = new Card();
        skaab.setName("Skaab Ruinator");
        skaab.setType(CardType.CREATURE);
        skaab.setManaCost("{1}{U}{U}");
        skaab.setPower(5);
        skaab.setToughness(6);
        skaab.addEffect(EffectSlot.SPELL, new com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost(3, CardType.CREATURE));

        gd.playerHands.get(aiPlayer.getId()).add(skaab);

        // Put 3 creature cards in graveyard
        for (int i = 0; i < 3; i++) {
            Card creature = new Card();
            creature.setName("Bear " + i);
            creature.setType(CardType.CREATURE);
            gd.playerGraveyards.get(aiPlayer.getId()).add(creature);
        }

        // Give AI enough mana
        gd.playerManaPools.get(aiPlayer.getId()).add(ManaColor.BLUE, 2);
        gd.playerManaPools.get(aiPlayer.getId()).add(ManaColor.COLORLESS, 1);

        createEngine().handleMessage("GAME_STATE", "");

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(eq(selfConnection), captor.capture());

        PlayCardRequest request = captor.getValue();
        assertThat(request.exileGraveyardCardIndices()).isNotNull();
        assertThat(request.exileGraveyardCardIndices()).hasSize(3);
        assertThat(request.exileGraveyardCardIndices()).containsExactly(0, 1, 2);
    }

    @Test
    @DisplayName("Easy AI selects only creature indices for ExileNCardsFromGraveyardCost in mixed graveyard")
    void selectsOnlyCreatureIndicesForExileNCostInMixedGraveyard() throws Exception {
        Card skaab = new Card();
        skaab.setName("Skaab Ruinator");
        skaab.setType(CardType.CREATURE);
        skaab.setManaCost("{1}{U}{U}");
        skaab.setPower(5);
        skaab.setToughness(6);
        skaab.addEffect(EffectSlot.SPELL, new com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost(3, CardType.CREATURE));

        gd.playerHands.get(aiPlayer.getId()).add(skaab);

        // Mixed graveyard: instant, creature, instant, creature, creature
        Card instant0 = new Card();
        instant0.setName("Spell 0");
        instant0.setType(CardType.INSTANT);
        gd.playerGraveyards.get(aiPlayer.getId()).add(instant0);

        Card creature1 = new Card();
        creature1.setName("Bear 1");
        creature1.setType(CardType.CREATURE);
        gd.playerGraveyards.get(aiPlayer.getId()).add(creature1);

        Card instant2 = new Card();
        instant2.setName("Spell 2");
        instant2.setType(CardType.INSTANT);
        gd.playerGraveyards.get(aiPlayer.getId()).add(instant2);

        Card creature3 = new Card();
        creature3.setName("Bear 3");
        creature3.setType(CardType.CREATURE);
        gd.playerGraveyards.get(aiPlayer.getId()).add(creature3);

        Card creature4 = new Card();
        creature4.setName("Bear 4");
        creature4.setType(CardType.CREATURE);
        gd.playerGraveyards.get(aiPlayer.getId()).add(creature4);

        gd.playerManaPools.get(aiPlayer.getId()).add(ManaColor.BLUE, 2);
        gd.playerManaPools.get(aiPlayer.getId()).add(ManaColor.COLORLESS, 1);

        createEngine().handleMessage("GAME_STATE", "");

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(eq(selfConnection), captor.capture());

        PlayCardRequest request = captor.getValue();
        assertThat(request.exileGraveyardCardIndices()).isNotNull();
        assertThat(request.exileGraveyardCardIndices()).hasSize(3);
        // Should pick indices 1, 3, 4 (the creature indices, skipping instants at 0 and 2)
        assertThat(request.exileGraveyardCardIndices()).containsExactly(1, 3, 4);
    }

    // ===== Mass damage spell evaluation =====

    @Test
    @DisplayName("Easy AI does not cast mass damage spell when no creatures are on the battlefield")
    void doesNotCastMassDamageWithNoCreatures() throws Exception {
        Card pyroclasm = new Card();
        pyroclasm.setName("Pyroclasm");
        pyroclasm.setType(CardType.SORCERY);
        pyroclasm.setManaCost("{1}{R}");
        pyroclasm.addEffect(EffectSlot.SPELL, new MassDamageEffect(2));
        gd.playerHands.get(aiPlayer.getId()).add(pyroclasm);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);
        pool.add(ManaColor.COLORLESS, 1);

        createEngine().handleMessage("GAME_STATE", "");

        verify(messageHandler, never()).handlePlayCard(any(), any());
        verify(messageHandler).handlePassPriority(any(), any());
    }

    @Test
    @DisplayName("Easy AI does not cast mass damage spell when it kills equal numbers of creatures on each side")
    void doesNotCastMassDamageOnEvenTrade() throws Exception {
        Card pyroclasm = new Card();
        pyroclasm.setName("Pyroclasm");
        pyroclasm.setType(CardType.SORCERY);
        pyroclasm.setManaCost("{1}{R}");
        pyroclasm.addEffect(EffectSlot.SPELL, new MassDamageEffect(2));
        gd.playerHands.get(aiPlayer.getId()).add(pyroclasm);

        UUID opponentId = gd.orderedPlayerIds.get(1);

        Card oppCard = new Card();
        oppCard.setName("Opp 2/2");
        oppCard.setType(CardType.CREATURE);
        oppCard.setPower(2);
        oppCard.setToughness(2);
        Permanent oppCreature = new Permanent(oppCard);
        gd.playerBattlefields.get(opponentId).add(oppCreature);

        Card aiCard = new Card();
        aiCard.setName("AI 2/2");
        aiCard.setType(CardType.CREATURE);
        aiCard.setPower(2);
        aiCard.setToughness(2);
        Permanent aiCreature = new Permanent(aiCard);
        gd.playerBattlefields.get(aiPlayer.getId()).add(aiCreature);

        when(gameQueryService.isCreature(gd, oppCreature)).thenReturn(true);
        when(gameQueryService.isCreature(gd, aiCreature)).thenReturn(true);
        when(gameQueryService.getEffectiveToughness(gd, oppCreature)).thenReturn(2);
        when(gameQueryService.getEffectiveToughness(gd, aiCreature)).thenReturn(2);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);
        pool.add(ManaColor.COLORLESS, 1);

        createEngine().handleMessage("GAME_STATE", "");

        verify(messageHandler, never()).handlePlayCard(any(), any());
        verify(messageHandler).handlePassPriority(any(), any());
    }

    @Test
    @DisplayName("Easy AI casts mass damage spell when it kills more opponent creatures than own")
    void castsMassDamageWhenKillsMoreOpponentCreatures() throws Exception {
        Card pyroclasm = new Card();
        pyroclasm.setName("Pyroclasm");
        pyroclasm.setType(CardType.SORCERY);
        pyroclasm.setManaCost("{1}{R}");
        pyroclasm.addEffect(EffectSlot.SPELL, new MassDamageEffect(2));
        gd.playerHands.get(aiPlayer.getId()).add(pyroclasm);

        UUID opponentId = gd.orderedPlayerIds.get(1);

        Card oppCard = new Card();
        oppCard.setName("Opp 2/2");
        oppCard.setType(CardType.CREATURE);
        oppCard.setPower(2);
        oppCard.setToughness(2);
        Permanent oppCreature = new Permanent(oppCard);
        gd.playerBattlefields.get(opponentId).add(oppCreature);

        when(gameQueryService.isCreature(gd, oppCreature)).thenReturn(true);
        when(gameQueryService.getEffectiveToughness(gd, oppCreature)).thenReturn(2);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);
        pool.add(ManaColor.COLORLESS, 1);

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any(), any());

        createEngine().handleMessage("GAME_STATE", "");

        verify(messageHandler).handlePlayCard(any(), any());
    }

    // ===== Entrancing Melody (PermanentManaValueEqualsXPredicate) — harness-based =====

    @Nested
    @DisplayName("Entrancing Melody co-selection of X and target")
    class EntrancingMelodyTests {

        private GameTestHarness testHarness;
        private Player human;
        private Player aiTestPlayer;
        private GameData testGd;
        private EasyAiDecisionEngine easyAi;

        @BeforeEach
        void setUpHarness() {
            testHarness = new GameTestHarness();
            human = testHarness.getPlayer1();
            aiTestPlayer = testHarness.getPlayer2();
            testGd = testHarness.getGameData();
            testHarness.skipMulligan();
            testHarness.clearMessages();

            FakeConnection aiConn = new FakeConnection("ai-easy-test");
            testHarness.getSessionManager().registerPlayer(aiConn, aiTestPlayer.getId(), "Bob");
            easyAi = new EasyAiDecisionEngine(testGd.id, aiTestPlayer, testHarness.getGameRegistry(),
                    testHarness.getMessageHandler(), testHarness.getGameQueryService(),
                    testHarness.getCombatAttackService(), testHarness.getGameBroadcastService(),
                    testHarness.getTargetValidationService());
            easyAi.setSelfConnection(aiConn);
        }

        private void giveAiPriorityLocal() {
            testHarness.forceActivePlayer(aiTestPlayer);
            testHarness.forceStep(TurnStep.PRECOMBAT_MAIN);
            testHarness.clearPriorityPassed();
            testGd.status = GameStatus.RUNNING;
            testGd.interaction.setAwaitingInput(null);
            testGd.stack.clear();
        }

        private void giveAiIslandsLocal(int count) {
            for (int i = 0; i < count; i++) {
                Permanent island = new Permanent(new Island());
                island.setSummoningSick(false);
                testGd.playerBattlefields.get(aiTestPlayer.getId()).add(island);
            }
        }

        @Test
        @DisplayName("Easy AI casts Entrancing Melody with X matching target's mana value")
        void castsEntrancingMelodyWithCorrectX() {
            giveAiPriorityLocal();
            giveAiIslandsLocal(4); // maxX = 2

            Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
            bears.setSummoningSick(false);
            testGd.playerBattlefields.get(human.getId()).add(bears);

            testHarness.setHand(aiTestPlayer, List.of(new EntrancingMelody()));

            easyAi.handleMessage("GAME_STATE", "");

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard().getName()).isEqualTo("Entrancing Melody");
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
            assertThat(testGd.stack.getFirst().getXValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("Easy AI picks highest affordable target for Entrancing Melody")
        void picksHighestAffordableTarget() {
            giveAiPriorityLocal();
            giveAiIslandsLocal(4); // maxX = 2

            Permanent vanguard = new Permanent(new EliteVanguard()); // MV=1
            vanguard.setSummoningSick(false);
            testGd.playerBattlefields.get(human.getId()).add(vanguard);

            Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
            bears.setSummoningSick(false);
            testGd.playerBattlefields.get(human.getId()).add(bears);

            testHarness.setHand(aiTestPlayer, List.of(new EntrancingMelody()));

            easyAi.handleMessage("GAME_STATE", "");

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
            assertThat(testGd.stack.getFirst().getXValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("Easy AI skips Entrancing Melody when target too expensive")
        void skipsEntrancingMelodyWhenTooExpensive() {
            giveAiPriorityLocal();
            giveAiIslandsLocal(3); // maxX = 1

            Permanent bears = new Permanent(new GrizzlyBears()); // MV=2, unaffordable
            bears.setSummoningSick(false);
            testGd.playerBattlefields.get(human.getId()).add(bears);

            testHarness.setHand(aiTestPlayer, List.of(new EntrancingMelody()));

            easyAi.handleMessage("GAME_STATE", "");

            assertThat(testGd.stack).isEmpty();
        }
    }

    // ===== Forced attack (Trove of Temptation) =====

    @Test
    @DisplayName("Easy AI attacks with at least one creature when forced by opponent effect")
    void attacksWithAtLeastOneWhenForcedByOpponentEffect() throws Exception {
        gd.currentStep = TurnStep.DECLARE_ATTACKERS;
        gd.interaction.setAwaitingInput(com.github.laxika.magicalvibes.model.AwaitingInput.ATTACKER_DECLARATION);

        // AI has a 2/2
        Permanent creature = new Permanent(new Card());
        creature.getCard().setName("Bear");
        creature.getCard().setType(CardType.CREATURE);
        creature.getCard().setPower(2);
        creature.getCard().setToughness(2);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(creature);

        // Opponent has a 5/5 blocker — AI would normally choose not to attack
        UUID opponentId = gd.orderedPlayerIds.get(1);
        Permanent blocker = new Permanent(new Card());
        blocker.getCard().setName("Big Blocker");
        blocker.getCard().setType(CardType.CREATURE);
        blocker.getCard().setPower(5);
        blocker.getCard().setToughness(5);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(opponentId).add(blocker);

        when(combatAttackService.getAttackableCreatureIndices(gd, aiPlayer.getId()))
                .thenReturn(List.of(0));
        when(combatAttackService.getMustAttackIndices(eq(gd), eq(aiPlayer.getId()), any()))
                .thenReturn(List.of());
        when(combatAttackService.isOpponentForcedToAttack(gd, aiPlayer.getId()))
                .thenReturn(true);
        when(gameBroadcastService.getAttackPaymentPerCreature(gd, aiPlayer.getId()))
                .thenReturn(0);
        when(gameQueryService.getEffectivePower(eq(gd), any())).thenReturn(2);
        when(gameQueryService.getEffectiveToughness(eq(gd), any())).thenReturn(2);
        when(gameQueryService.canBlock(gd, blocker)).thenReturn(true);
        when(gameQueryService.canBlockAttacker(eq(gd), eq(blocker), any(), any())).thenReturn(true);
        when(gameQueryService.getEffectivePower(gd, blocker)).thenReturn(5);
        when(gameQueryService.getEffectiveToughness(gd, blocker)).thenReturn(5);

        createEngine().handleMessage("AVAILABLE_ATTACKERS", "");

        ArgumentCaptor<DeclareAttackersRequest> captor = ArgumentCaptor.forClass(DeclareAttackersRequest.class);
        verify(messageHandler).handleDeclareAttackers(eq(selfConnection), captor.capture());

        // Must declare at least one attacker despite unfavorable board
        assertThat(captor.getValue().attackerIndices()).isNotEmpty();
    }

    @Test
    @DisplayName("Easy AI can declare zero attackers when not forced by opponent effect")
    void canDeclareZeroAttackersWhenNotForced() throws Exception {
        gd.currentStep = TurnStep.DECLARE_ATTACKERS;
        gd.interaction.setAwaitingInput(com.github.laxika.magicalvibes.model.AwaitingInput.ATTACKER_DECLARATION);

        // AI has a 2/2 but strong opponent blocker — Easy AI should choose not to attack
        Permanent creature = new Permanent(new Card());
        creature.getCard().setName("Bear");
        creature.getCard().setType(CardType.CREATURE);
        creature.getCard().setPower(2);
        creature.getCard().setToughness(2);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(creature);

        UUID opponentId = gd.orderedPlayerIds.get(1);
        Permanent blocker = new Permanent(new Card());
        blocker.getCard().setName("Big Blocker");
        blocker.getCard().setType(CardType.CREATURE);
        blocker.getCard().setPower(5);
        blocker.getCard().setToughness(5);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(opponentId).add(blocker);

        when(combatAttackService.getAttackableCreatureIndices(gd, aiPlayer.getId()))
                .thenReturn(List.of(0));
        when(combatAttackService.getMustAttackIndices(eq(gd), eq(aiPlayer.getId()), any()))
                .thenReturn(List.of());
        when(combatAttackService.isOpponentForcedToAttack(gd, aiPlayer.getId()))
                .thenReturn(false);
        when(gameBroadcastService.getAttackPaymentPerCreature(gd, aiPlayer.getId()))
                .thenReturn(0);
        when(gameQueryService.getEffectivePower(eq(gd), any())).thenReturn(2);
        when(gameQueryService.getEffectiveToughness(eq(gd), any())).thenReturn(2);
        when(gameQueryService.canBlock(gd, blocker)).thenReturn(true);
        when(gameQueryService.canBlockAttacker(eq(gd), eq(blocker), any(), any())).thenReturn(true);
        when(gameQueryService.getEffectivePower(gd, blocker)).thenReturn(5);
        when(gameQueryService.getEffectiveToughness(gd, blocker)).thenReturn(5);

        createEngine().handleMessage("AVAILABLE_ATTACKERS", "");

        ArgumentCaptor<DeclareAttackersRequest> captor = ArgumentCaptor.forClass(DeclareAttackersRequest.class);
        verify(messageHandler).handleDeclareAttackers(eq(selfConnection), captor.capture());

        // Without forced attack, AI should choose zero attackers (unfavorable trade)
        assertThat(captor.getValue().attackerIndices()).isEmpty();
    }

    // ===== tapManaForSpell awaiting input (mana ability triggers color choice) =====

    @Test
    @DisplayName("Easy AI does not cast spell when mana tapping triggers awaiting input")
    void doesNotCastSpellWhenManaTappingTriggersAwaitingInput() throws Exception {
        Card creature = new Card();
        creature.setName("Test Knight");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{W}");
        creature.setPower(2);
        creature.setToughness(2);
        gd.playerHands.get(aiPlayer.getId()).add(creature);

        // Add an untapped Plains to the battlefield so AI needs to tap it for mana
        Permanent land = new Permanent(new Plains());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(land);

        // Allow tapping flow to proceed
        when(gameQueryService.canActivateManaAbility(any(), any())).thenReturn(true);

        // Simulate mana ability triggering awaiting input (e.g. Treasure color choice)
        Mockito.doAnswer(inv -> {
            gd.interaction.setAwaitingInput(AwaitingInput.COLOR_CHOICE);
            return null;
        }).when(messageHandler).handleTapPermanent(any(), any());

        createEngine().handleMessage("GAME_STATE", "");

        // AI should have tapped the land but NOT cast the spell or passed priority
        verify(messageHandler).handleTapPermanent(any(), any());
        verify(messageHandler, never()).handlePlayCard(any(), any());
        verify(messageHandler, never()).handlePassPriority(any(), any());
    }

    @Test
    @DisplayName("Easy AI does not cast instant when mana tapping triggers awaiting input")
    void doesNotCastInstantWhenManaTappingTriggersAwaitingInput() throws Exception {
        // Use opponent's turn so the instant-casting path is used
        gd.currentStep = TurnStep.END_STEP;
        UUID opponentId = gd.orderedPlayerIds.get(1);
        gd.activePlayerId = opponentId;
        gd.stack.clear();
        // Opponent has already passed priority, so AI holds priority
        gd.priorityPassedBy.add(opponentId);

        Card instant = new Card();
        instant.setName("Test Bolt");
        instant.setType(CardType.INSTANT);
        instant.setManaCost("{W}");
        gd.playerHands.get(aiPlayer.getId()).add(instant);

        Permanent land = new Permanent(new Plains());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(land);

        when(gameQueryService.canActivateManaAbility(any(), any())).thenReturn(true);

        Mockito.doAnswer(inv -> {
            gd.interaction.setAwaitingInput(AwaitingInput.COLOR_CHOICE);
            return null;
        }).when(messageHandler).handleTapPermanent(any(), any());

        createEngine().handleMessage("GAME_STATE", "");

        verify(messageHandler).handleTapPermanent(any(), any());
        verify(messageHandler, never()).handlePlayCard(any(), any());
        verify(messageHandler, never()).handlePassPriority(any(), any());
    }
}
