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
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.PermanentView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.networking.service.GameLogViewFactory;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

@ExtendWith(MockitoExtension.class)
class GameBroadcastServiceTest {

    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private PermanentViewFactory permanentViewFactory;
    @Mock private StackEntryViewFactory stackEntryViewFactory;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameLogViewFactory gameLogViewFactory;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private ValidTargetService validTargetService;
    @Mock private com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService conditionEvaluationService;

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
                support, gameQueryService, predicateEvaluationService, conditionEvaluationService,
                new com.github.laxika.magicalvibes.service.effect.cost.AdditionalSpellCostService(
                        gameQueryService, predicateEvaluationService));
        CastingPermissionService castingPermissionService =
                new CastingPermissionService(gameQueryService, predicateEvaluationService, conditionEvaluationService);
        svc = new GameBroadcastService(sessionManager, cardViewFactory, gameLogViewFactory, permanentViewFactory,
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

    @Nested
    @DisplayName("getPotentialPayableAbilityIndices — abilities payable after tapping mana sources")
    class GetPotentialPayableAbilityIndicesTests {

        private Permanent manaLand(com.github.laxika.magicalvibes.model.ManaColor color) {
            Card card = new Card();
            card.setName(color + " land");
            card.setType(CardType.LAND);
            card.addEffect(EffectSlot.ON_TAP,
                    new com.github.laxika.magicalvibes.model.effect.AwardManaEffect(color));
            return new Permanent(card);
        }

        private Permanent abilitySource(String name, com.github.laxika.magicalvibes.model.ActivatedAbility ability) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.CREATURE);
            card.addActivatedAbility(ability);
            return new Permanent(card);
        }

        @Test
        @DisplayName("Ability is listed when untapped sources can produce its colors")
        void abilityPayableWhenSourceColorsMatch() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);
            when(gameQueryService.canActivateManaAbility(same(gd), any())).thenReturn(true);

            Permanent source = abilitySource("Twinblade Paladin",
                    new com.github.laxika.magicalvibes.model.ActivatedAbility(
                            false, "{W}{W}", List.of(), "gain double strike"));
            gd.playerBattlefields.get(player1Id).add(source);
            gd.playerBattlefields.get(player1Id).add(manaLand(com.github.laxika.magicalvibes.model.ManaColor.WHITE));
            gd.playerBattlefields.get(player1Id).add(manaLand(com.github.laxika.magicalvibes.model.ManaColor.WHITE));

            assertThat(svc.getPotentialPayableAbilityIndices(gd, player1Id))
                    .containsExactly(java.util.Map.entry(source.getId(), List.of(0)));
        }

        @Test
        @DisplayName("Ability is not listed when the source colors can't meet the cost")
        void abilityNotPayableWhenColorsCantMeet() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);
            when(gameQueryService.canActivateManaAbility(same(gd), any())).thenReturn(true);

            Permanent source = abilitySource("Twinblade Paladin",
                    new com.github.laxika.magicalvibes.model.ActivatedAbility(
                            false, "{W}{W}", List.of(), "gain double strike"));
            gd.playerBattlefields.get(player1Id).add(source);
            gd.playerBattlefields.get(player1Id).add(manaLand(com.github.laxika.magicalvibes.model.ManaColor.GREEN));
            gd.playerBattlefields.get(player1Id).add(manaLand(com.github.laxika.magicalvibes.model.ManaColor.GREEN));

            assertThat(svc.getPotentialPayableAbilityIndices(gd, player1Id)).isEmpty();
        }

        @Test
        @DisplayName("A {T}-cost ability can't count its own source's mana toward the cost")
        void tapCostAbilityExcludesOwnSourceMana() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);
            when(gameQueryService.canActivateManaAbility(same(gd), any())).thenReturn(true);

            // A Rishadan Port-style land: "{T}: Add {C}" plus "{1}, {T}: tap target land".
            Card port = new Card();
            port.setName("Rishadan Port");
            port.setType(CardType.LAND);
            port.addEffect(EffectSlot.ON_TAP,
                    new com.github.laxika.magicalvibes.model.effect.AwardManaEffect(
                            com.github.laxika.magicalvibes.model.ManaColor.COLORLESS));
            port.addActivatedAbility(new com.github.laxika.magicalvibes.model.ActivatedAbility(
                    true, "{1}", List.of(), "tap target land"));
            Permanent portPerm = new Permanent(port);
            gd.playerBattlefields.get(player1Id).add(portPerm);

            // Alone, the Port's own mana can't pay its own {T} ability
            assertThat(svc.getPotentialPayableAbilityIndices(gd, player1Id)).isEmpty();

            // A second mana source makes the ability payable
            gd.playerBattlefields.get(player1Id).add(manaLand(com.github.laxika.magicalvibes.model.ManaColor.GREEN));
            assertThat(svc.getPotentialPayableAbilityIndices(gd, player1Id))
                    .containsExactly(java.util.Map.entry(portPerm.getId(), List.of(0)));
        }

        @Test
        @DisplayName("Abilities without a mana cost are omitted")
        void abilitiesWithoutManaCostOmitted() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);
            when(gameQueryService.canActivateManaAbility(same(gd), any())).thenReturn(true);

            Permanent source = abilitySource("Pendelhaven",
                    new com.github.laxika.magicalvibes.model.ActivatedAbility(
                            true, null, List.of(), "pump a 1/1"));
            gd.playerBattlefields.get(player1Id).add(source);
            gd.playerBattlefields.get(player1Id).add(manaLand(com.github.laxika.magicalvibes.model.ManaColor.GREEN));

            assertThat(svc.getPotentialPayableAbilityIndices(gd, player1Id)).isEmpty();
        }

        @Test
        @DisplayName("Floating mana already in the pool counts toward payability")
        void floatingPoolCountsTowardPayable() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);
            when(gameQueryService.canActivateManaAbility(same(gd), any())).thenReturn(true);

            Permanent source = abilitySource("Steel Overseer",
                    new com.github.laxika.magicalvibes.model.ActivatedAbility(
                            false, "{2}", List.of(), "grow artifacts"));
            gd.playerBattlefields.get(player1Id).add(source);
            gd.playerManaPools.get(player1Id).add(com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

            assertThat(svc.getPotentialPayableAbilityIndices(gd, player1Id))
                    .containsExactly(java.util.Map.entry(source.getId(), List.of(0)));
        }

        @Test
        @DisplayName("Empty for a player who does not hold priority")
        void emptyWithoutPriority() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player2Id);

            Permanent source = abilitySource("Twinblade Paladin",
                    new com.github.laxika.magicalvibes.model.ActivatedAbility(
                            false, "{W}{W}", List.of(), "gain double strike"));
            gd.playerBattlefields.get(player1Id).add(source);

            assertThat(svc.getPotentialPayableAbilityIndices(gd, player1Id)).isEmpty();
        }
    }

    @Nested
    @DisplayName("logAndBroadcast — structured game log entries")
    class LogAndBroadcastTests {

        @Test
        @DisplayName("plain string logs as a single text segment")
        void plainStringLog() {
            svc.logAndBroadcast(gd, GameLogEntry.text("Game started!"));

            assertThat(gd.gameLog).containsExactly(GameLogEntry.text("Game started!"));
            assertThat(gd.gameLog.getFirst().segments()).hasSize(1);
        }

        @Test
        @DisplayName("structured entry preserves card segment")
        void structuredCardLog() {
            Card bolt = new Card();
            bolt.setName("Lightning Bolt");

            svc.logAndBroadcast(gd, GameLog.builder()
                    .text("Player1 casts ")
                    .card(bolt)
                    .text(".")
                    .build());

            assertThat(gd.gameLog).hasSize(1);
            assertThat(gd.gameLog.getFirst().plainText()).isEqualTo("Player1 casts Lightning Bolt.");
            assertThat(gd.gameLog.getFirst().segments()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("getBattlefields — cards exiled with a permanent, shown tucked under it")
    class ExiledWithDisplayTests {

        private Permanent sourcePermanent;

        @BeforeEach
        void addSourcePermanent() {
            Card source = artifact("Mimic Vat");
            sourcePermanent = new Permanent(source);
            gd.playerBattlefields.get(player1Id).add(sourcePermanent);
        }

        private Card artifact(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.ARTIFACT);
            return card;
        }

        @Test
        @DisplayName("imprinted card still in exile is listed under its permanent")
        void imprintedCardListedWhileInExile() {
            Card imprinted = artifact("Gravecrawler");
            gd.addToExile(player1Id, imprinted);
            gd.setImprintedCard(sourcePermanent.getCard(), imprinted);

            assertThat(gd.getExiledWithPermanentEntries(sourcePermanent.getId(), sourcePermanent.getCard().getId()))
                    .extracting(com.github.laxika.magicalvibes.model.ExiledCardEntry::card)
                    .containsExactly(imprinted);
        }

        @Test
        @DisplayName("imprint map entry whose card left exile is not listed")
        void imprintedCardNoLongerInExileNotListed() {
            Card imprinted = artifact("Gravecrawler");
            gd.setImprintedCard(sourcePermanent.getCard(), imprinted);
            // Card was never added to exile (e.g. already played from exile)

            assertThat(gd.getExiledWithPermanentEntries(sourcePermanent.getId(), sourcePermanent.getCard().getId()))
                    .isEmpty();
        }

        @Test
        @DisplayName("card recorded both as imprint and source-tracked exile (O-Ring style) appears once")
        void imprintAlsoSourceTrackedListedOnce() {
            Card exiled = artifact("Bound Permanent");
            gd.addToExile(player1Id, exiled, sourcePermanent.getId());
            gd.setImprintedCard(sourcePermanent.getCard(), exiled);

            assertThat(gd.getExiledWithPermanentEntries(sourcePermanent.getId(), sourcePermanent.getCard().getId()))
                    .extracting(com.github.laxika.magicalvibes.model.ExiledCardEntry::card)
                    .containsExactly(exiled);
        }

        @Test
        @DisplayName("face-down entries keep their flag (hideaway imprint and source-tracked exiles)")
        void faceDownEntriesKeepTheirFlag() {
            // Hideaway-style: imprinted face down, exile entry has no source id
            Card hideawayCard = artifact("Hidden Prize");
            gd.addToExile(player1Id, hideawayCard, null, true);
            gd.setImprintedCard(sourcePermanent.getCard(), hideawayCard);
            // Grimoire Thief-style: source-tracked face-down exile
            Card stolenCard = artifact("Stolen Secret");
            gd.addToExile(player2Id, stolenCard, sourcePermanent.getId(), true);

            assertThat(gd.getExiledWithPermanentEntries(sourcePermanent.getId(), sourcePermanent.getCard().getId()))
                    .allMatch(com.github.laxika.magicalvibes.model.ExiledCardEntry::faceDown)
                    .extracting(com.github.laxika.magicalvibes.model.ExiledCardEntry::card)
                    .containsExactly(hideawayCard, stolenCard);
        }

        private CardView cardViewNamed(String name) {
            return new CardView(UUID.randomUUID(), name, null, Set.of(), Set.of(), List.of(), null, null,
                    null, null, Set.of(), false, null, null, null, List.of(), false, false, List.of(), null,
                    false, false, 0, false, null, false, 0, 0, 0, null, List.of(), List.of(), false, null,
                    0, false, List.of());
        }

        private PermanentView permanentView(UUID permId, int faceDownCount) {
            return new PermanentView(permId, null, false, false, false, List.of(), false, 0, 0, Set.of(),
                    Set.of(), 0, 0, null, null, null, 0, false, false, java.util.Map.of(), null, 0, false,
                    false, List.of(), List.of(), faceDownCount, List.of());
        }

        @Test
        @DisplayName("face-down exiled cards are collected as a reveal for the permanent's controller")
        void faceDownCardsCollectedForController() {
            Card hidden = artifact("Hidden Prize");
            gd.addToExile(player1Id, hidden, sourcePermanent.getId(), true);
            CardView hiddenView = cardViewNamed("Hidden Prize");
            when(cardViewFactory.create(hidden)).thenReturn(hiddenView);

            var reveals = svc.collectFaceDownReveals(gd);

            assertThat(reveals).containsOnlyKeys(sourcePermanent.getId());
            assertThat(reveals.get(sourcePermanent.getId()).viewerId()).isEqualTo(player1Id);
            assertThat(reveals.get(sourcePermanent.getId()).cards()).containsExactly(hiddenView);
        }

        @Test
        @DisplayName("face-up exiles produce no reveal entry")
        void faceUpExilesProduceNoReveal() {
            gd.addToExile(player1Id, artifact("Shown Card"), sourcePermanent.getId());

            assertThat(svc.collectFaceDownReveals(gd)).isEmpty();
        }

        @Test
        @DisplayName("the controller's view gets the face-down cards; the opponent keeps only the count")
        void faceDownCardsRevealedOnlyToController() {
            PermanentView shared = permanentView(sourcePermanent.getId(), 1);
            List<List<PermanentView>> battlefields = List.of(List.of(shared), List.of());
            CardView hiddenView = cardViewNamed("Hidden Prize");
            var reveals = java.util.Map.of(sourcePermanent.getId(),
                    new GameBroadcastService.FaceDownReveal(player1Id, List.of(hiddenView)));

            PermanentView controllerView = svc.applyFaceDownReveals(battlefields, reveals, player1Id)
                    .getFirst().getFirst();
            assertThat(controllerView.faceDownExiledCards()).containsExactly(hiddenView);
            assertThat(controllerView.faceDownExiledCount()).isZero();

            PermanentView opponentView = svc.applyFaceDownReveals(battlefields, reveals, player2Id)
                    .getFirst().getFirst();
            assertThat(opponentView).isSameAs(shared);
            assertThat(opponentView.faceDownExiledCards()).isEmpty();
            assertThat(opponentView.faceDownExiledCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("battlefield views receive face-up cards and the face-down count separately")
        void battlefieldViewSplitsFaceUpAndFaceDown() {
            Card faceUpCard = artifact("Welded Artifact");
            gd.addToExile(player1Id, faceUpCard, sourcePermanent.getId());
            Card hiddenCard = artifact("Hidden Card");
            gd.addToExile(player1Id, hiddenCard, sourcePermanent.getId(), true);

            GameQueryService.StaticBonus noBonus = new GameQueryService.StaticBonus(
                    0, 0, Set.of(), Set.of(), false, List.of(), List.of(), Set.of(), List.of(), Set.of(),
                    Set.of(), false, false, false, Set.of(), false, 0, 0, false, false);
            when(gameQueryService.explainStaticBonus(gd, sourcePermanent))
                    .thenReturn(new GameQueryService.ExplainedBonus(noBonus, List.of()));

            svc.getBattlefields(gd);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Card>> faceUpCaptor = ArgumentCaptor.forClass(List.class);
            verify(permanentViewFactory).create(same(sourcePermanent), anyInt(), anyInt(), any(), anyBoolean(),
                    anyList(), any(), anyList(), any(), anyBoolean(), anyBoolean(), anyBoolean(), any(),
                    anyBoolean(), any(), anyList(), faceUpCaptor.capture(), eq(1));
            assertThat(faceUpCaptor.getValue()).containsExactly(faceUpCard);
        }
    }
}
