package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        EasyAiDecisionEngine engine = new EasyAiDecisionEngine(
                gd.id, aiPlayer, gameRegistry, messageHandler,
                gameQueryService, combatAttackService);
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
}
