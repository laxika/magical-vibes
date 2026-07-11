package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCastCostEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostForCardTypeEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.CastingCostService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.cast.CostModificationTestRegistry;
import com.github.laxika.magicalvibes.service.cast.CostModificationSupport;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@ExtendWith(MockitoExtension.class)
class GameBroadcastServiceTest {

    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private PermanentViewFactory permanentViewFactory;
    @Mock private StackEntryViewFactory stackEntryViewFactory;
    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private ValidTargetService validTargetService;

    private GameBroadcastService svc;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        // Real casting services (with the real handler registry) over the mocked collaborators,
        // so the playable-index computation exercises the same cost/permission code paths as production.
        CostModificationSupport support = new CostModificationSupport(gameQueryService, predicateEvaluationService);
        CastingCostService castingCostService = new CastingCostService(
                CostModificationTestRegistry.build(gameQueryService, predicateEvaluationService, support),
                support, gameQueryService, predicateEvaluationService);
        CastingPermissionService castingPermissionService =
                new CastingPermissionService(gameQueryService, predicateEvaluationService);
        svc = new GameBroadcastService(sessionManager, cardViewFactory, permanentViewFactory,
                stackEntryViewFactory, gameQueryService, validTargetService,
                castingCostService, castingPermissionService,
                new com.github.laxika.magicalvibes.service.cast.PotentialManaService(gameQueryService));

        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerLifeTotals.put(player1Id, 20);
        gd.playerLifeTotals.put(player2Id, 20);
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
    }

    @Nested
    @DisplayName("isCardPlayable — pure single-card query")
    class IsCardPlayableTests {

        private Card simpleCreature(String name, String manaCost) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.CREATURE);
            card.setManaCost(manaCost);
            return card;
        }

        @Test
        @DisplayName("Checks affordability against the provided pool, not the player's actual pool")
        void usesProvidedPool() {
            Card creature = simpleCreature("Centaur Courser", "{2}{G}");
            // Player's actual pool is empty

            ManaPool hypothetical = new ManaPool();
            hypothetical.add(com.github.laxika.magicalvibes.model.ManaColor.GREEN);
            hypothetical.add(com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

            assertThat(svc.isCardPlayable(gd, player1Id, creature, hypothetical, 0)).isTrue();
            assertThat(svc.isCardPlayable(gd, player1Id, creature,
                    gd.playerManaPools.get(player1Id), 0)).isFalse();
            // The query never spends from the provided pool
            assertThat(hypothetical.getTotal()).isEqualTo(3);
        }

        @Test
        @DisplayName("additionalGenericCost is added on top of the mana cost (targeting tax)")
        void additionalGenericCostApplies() {
            Card creature = simpleCreature("Centaur Courser", "{2}{G}");

            ManaPool exactPool = new ManaPool();
            exactPool.add(com.github.laxika.magicalvibes.model.ManaColor.GREEN);
            exactPool.add(com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

            assertThat(svc.isCardPlayable(gd, player1Id, creature, exactPool, 0)).isTrue();
            assertThat(svc.isCardPlayable(gd, player1Id, creature, exactPool, 1)).isFalse();
        }

        @Test
        @DisplayName("Does not apply priority gating — callers evaluate hypothetical states")
        void noPriorityGating() {
            // getPriorityPlayerId is never stubbed: the single-card query must not consult it
            Card creature = simpleCreature("Centaur Courser", "{2}{G}");

            ManaPool pool = new ManaPool();
            pool.add(com.github.laxika.magicalvibes.model.ManaColor.GREEN);
            pool.add(com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

            assertThat(svc.isCardPlayable(gd, player1Id, creature, pool, 0)).isTrue();
        }

        @Test
        @DisplayName("Agrees with getPlayableCardIndices membership for every hand card")
        void agreesWithPlayableList() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);

            gd.playerHands.get(player1Id).add(simpleCreature("Affordable", "{G}"));
            gd.playerHands.get(player1Id).add(simpleCreature("Unaffordable", "{5}{G}"));
            gd.playerManaPools.get(player1Id).add(com.github.laxika.magicalvibes.model.ManaColor.GREEN);

            List<Integer> playable = svc.getPlayableCardIndices(gd, player1Id, 0);

            for (int i = 0; i < gd.playerHands.get(player1Id).size(); i++) {
                assertThat(svc.isCardPlayable(gd, player1Id, gd.playerHands.get(player1Id).get(i),
                        gd.playerManaPools.get(player1Id), 0))
                        .as("card %d agrees with playable list", i)
                        .isEqualTo(playable.contains(i));
            }
            assertThat(playable).containsExactly(0);
        }
    }

    @Nested
    @DisplayName("getPlayableCardIndices — uses casting services for cost modifiers")
    class GetPlayableCardIndicesTests {

        @Test
        @DisplayName("Creature with cost reduction becomes playable")
        void creatureWithCostReductionIsPlayable() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);

            // Heartless Summoning: creatures cost {2} less
            Card reducer = new Card();
            reducer.setName("Heartless Summoning");
            reducer.setType(CardType.ENCHANTMENT);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceOwnCastCostForCardTypeEffect(Set.of(CardType.CREATURE), new Fixed(2)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(reducer));

            // Player has {G} and a {2}{G} creature — with -2 reduction, effective cost is {G} only
            gd.playerManaPools.get(player1Id).add(com.github.laxika.magicalvibes.model.ManaColor.GREEN);

            Card creature = new Card();
            creature.setName("Centaur Courser");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{2}{G}");
            gd.playerHands.get(player1Id).add(creature);

            List<Integer> playable = svc.getPlayableCardIndices(gd, player1Id, 0);

            assertThat(playable).contains(0);
        }

        @Test
        @DisplayName("Over-reduction floors at colored pips: playable with only the colored mana, not with an empty pool")
        void overReductionFloorsAtColoredPips() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);

            // Reducer discounts creatures by {5} — more than this creature's {2} generic portion.
            Card reducer = new Card();
            reducer.setName("Big Reducer");
            reducer.setType(CardType.ENCHANTMENT);
            reducer.addEffect(EffectSlot.STATIC,
                    new ReduceOwnCastCostForCardTypeEffect(Set.of(CardType.CREATURE), new Fixed(5)));
            gd.playerBattlefields.get(player1Id).add(new Permanent(reducer));

            Card creature = new Card();
            creature.setName("Centaur Courser");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{2}{G}");
            gd.playerHands.get(player1Id).add(creature);

            // Empty pool: the over-reduction must NOT pay the {G} pip — unplayable.
            assertThat(svc.getPlayableCardIndices(gd, player1Id, 0)).doesNotContain(0);

            // A single {G}: colored pip satisfied, generic floored to 0 — playable.
            gd.playerManaPools.get(player1Id).add(com.github.laxika.magicalvibes.model.ManaColor.GREEN);
            assertThat(svc.getPlayableCardIndices(gd, player1Id, 0)).contains(0);
        }

        @Test
        @DisplayName("Spell taxed by opponent permanent becomes unplayable without enough mana")
        void spellTaxedByOpponentBecomesUnplayable() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);

            // Opponent has Thalia: instants/sorceries cost {1} more
            Card thalia = new Card();
            thalia.setName("Thalia");
            thalia.setType(CardType.CREATURE);
            thalia.addEffect(EffectSlot.STATIC,
                    new IncreaseOpponentCastCostEffect(Set.of(CardType.INSTANT, CardType.SORCERY), 1));
            gd.playerBattlefields.get(player2Id).add(new Permanent(thalia));

            // Player has exactly {R} — Lightning Bolt costs {R} + {1} tax = can't afford
            gd.playerManaPools.get(player1Id).add(com.github.laxika.magicalvibes.model.ManaColor.RED);

            Card bolt = new Card();
            bolt.setName("Lightning Bolt");
            bolt.setType(CardType.INSTANT);
            bolt.setManaCost("{R}");
            gd.playerHands.get(player1Id).add(bolt);

            List<Integer> playable = svc.getPlayableCardIndices(gd, player1Id, 0);

            assertThat(playable).doesNotContain(0);
        }

        @Test
        @DisplayName("Land is playable during main phase regardless of cost modifiers")
        void landPlayableDuringMainPhase() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);

            Card land = new Card();
            land.setName("Forest");
            land.setType(CardType.LAND);
            gd.playerHands.get(player1Id).add(land);

            List<Integer> playable = svc.getPlayableCardIndices(gd, player1Id, 0);

            assertThat(playable).contains(0);
        }
    }
}
