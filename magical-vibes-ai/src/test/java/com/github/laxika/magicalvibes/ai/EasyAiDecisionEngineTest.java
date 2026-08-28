package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AbandonHope;
import com.github.laxika.magicalvibes.cards.a.AjanisResponse;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EkunduCyclops;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.e.Errantry;
import com.github.laxika.magicalvibes.cards.e.Evangelize;
import com.github.laxika.magicalvibes.cards.f.FinalShowdown;
import com.github.laxika.magicalvibes.cards.f.FaithOfTheDevoted;
import com.github.laxika.magicalvibes.cards.f.FinaleOfPromise;
import com.github.laxika.magicalvibes.cards.f.FlameblastDragon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HootingMandrills;
import com.github.laxika.magicalvibes.cards.h.Hipparion;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.b.BorrowedHostility;
import com.github.laxika.magicalvibes.cards.b.BasalThrull;
import com.github.laxika.magicalvibes.cards.b.BlindingBeam;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AlphaAuthority;
import com.github.laxika.magicalvibes.cards.a.ArchangelOfTithes;
import com.github.laxika.magicalvibes.cards.a.AshlingsCommand;
import com.github.laxika.magicalvibes.cards.c.ChampionOfThePath;
import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.c.CallerOfTheHunt;
import com.github.laxika.magicalvibes.cards.c.CrypticCommand;
import com.github.laxika.magicalvibes.cards.c.CostlyPlunder;
import com.github.laxika.magicalvibes.cards.c.CurseOfEchoes;
import com.github.laxika.magicalvibes.cards.c.Crawlspace;
import com.github.laxika.magicalvibes.cards.d.Dominate;
import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.d.Derelor;
import com.github.laxika.magicalvibes.cards.d.Drought;
import com.github.laxika.magicalvibes.cards.d.DreamHalls;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.k.KjeldoranRoyalGuard;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.Lure;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OrcishConscripts;
import com.github.laxika.magicalvibes.cards.o.Okk;
import com.github.laxika.magicalvibes.cards.o.OpenTheWay;
import com.github.laxika.magicalvibes.cards.o.Opportunity;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PhyrexianPurge;
import com.github.laxika.magicalvibes.cards.p.PrimitiveJustice;
import com.github.laxika.magicalvibes.cards.p.Pyrokinesis;
import com.github.laxika.magicalvibes.cards.p.PyrrhicStrike;
import com.github.laxika.magicalvibes.cards.r.ReignOfChaos;
import com.github.laxika.magicalvibes.cards.r.Ramroller;
import com.github.laxika.magicalvibes.cards.r.RiskFactor;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.SufferThePast;
import com.github.laxika.magicalvibes.cards.s.SelectiveSnare;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StrengthOfTheTajuru;
import com.github.laxika.magicalvibes.cards.t.Tromokratis;
import com.github.laxika.magicalvibes.cards.t.TolarianScholar;
import com.github.laxika.magicalvibes.cards.t.ToralfGodOfFury;
import com.github.laxika.magicalvibes.cards.t.TorgaarFamineIncarnate;
import com.github.laxika.magicalvibes.cards.t.TorrentOfSouls;
import com.github.laxika.magicalvibes.cards.t.TroveOfTemptation;
import com.github.laxika.magicalvibes.cards.u.Unbury;
import com.github.laxika.magicalvibes.cards.u.UrgentNecropsy;
import com.github.laxika.magicalvibes.cards.v.Victimize;
import com.github.laxika.magicalvibes.cards.w.WearTear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentOrDiscardCardCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityContext;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("scryfall")
@ExtendWith(MockitoExtension.class)
class EasyAiDecisionEngineTest {

    @Mock private AiGameActions messageHandler;
    @Mock private GameQueryService gameQueryService;
    @Mock private BlockLegalityService blockLegalityService;
    @Mock private CombatAttackService combatAttackService;
    @Mock private GameActionAvailabilityService actionAvailabilityService;
    @Mock private com.github.laxika.magicalvibes.service.cast.CastingCostService castingCostService;
    @Mock private com.github.laxika.magicalvibes.service.cast.CastingPermissionService castingPermissionService;
    @Mock private com.github.laxika.magicalvibes.service.effect.TargetValidationService targetValidationService;

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
        Mockito.lenient().when(combatAttackService.getMaximumAttackers(gd))
                .thenReturn(Integer.MAX_VALUE);

        gameRegistry = new GameRegistry();
        gameRegistry.register(gd);

        Mockito.lenient().when(combatAttackService.getMaximumAttackers(
                any(GameData.class), any(UUID.class))).thenReturn(Integer.MAX_VALUE);
        Mockito.lenient().doAnswer(invocation -> {
            java.util.function.Supplier<?> queries = invocation.getArgument(1);
            return queries.get();
        }).when(gameQueryService).withQueryScope(any(GameData.class), any());
    }

    private EasyAiDecisionEngine createEngine() {
        AiTestPlayabilityStub.install(actionAvailabilityService, castingCostService, gameQueryService);
        EasyAiDecisionEngine engine = new EasyAiDecisionEngine(
                gd.id, aiPlayer, gameRegistry, messageHandler,
                gameQueryService, blockLegalityService, combatAttackService, actionAvailabilityService,
                castingCostService, castingPermissionService,
                targetValidationService,
                new com.github.laxika.magicalvibes.service.target.TargetLegalityService(gameQueryService,
                        new com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService(gameQueryService),
                        targetValidationService,
                        Mockito.mock(com.github.laxika.magicalvibes.service.effect.AmountEvaluationService.class),
                        new com.github.laxika.magicalvibes.service.target.TargetGroupAssignmentService(gameQueryService)));
        return engine;
    }

    private Card whitePlainsCreature() {
        Card card = new Card();
        card.setName("White Plains Creature");
        card.setType(CardType.LAND);
        card.setAdditionalTypes(Set.of(CardType.CREATURE));
        card.setColors(List.of(CardColor.WHITE));
        card.setSubtypes(List.of(CardSubtype.PLAINS));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Test
    @DisplayName("Easy AI uses Delve cards to reduce a spell's generic mana cost")
    void castsDelveSpellWithGraveyardReduction() {
        GameTestHarness testHarness = new GameTestHarness();
        Player testAiPlayer = testHarness.getPlayer2();
        GameData testGameData = testHarness.getGameData();
        testHarness.skipMulligan();

        FakeConnection aiConnection = new FakeConnection("ai-easy-delve-test");
        testHarness.getSessionManager().registerPlayer(
                aiConnection, testAiPlayer.getId(), testAiPlayer.getUsername());
        EasyAiDecisionEngine testAi = new EasyAiDecisionEngine(
                testGameData.id, testAiPlayer, testHarness.getGameRegistry(), testHarness.getGameService(),
                testHarness.getGameQueryService(), testHarness.getBlockLegalityService(),
                testHarness.getCombatAttackService(), testHarness.getGameActionAvailabilityService(),
                testHarness.getCastingCostService(), testHarness.getCastingPermissionService(),
                testHarness.getTargetValidationService(), testHarness.getTargetLegalityService());

        for (int i = 0; i < 4; i++) {
            Permanent forest = testHarness.addToBattlefieldAndReturn(testAiPlayer, new Forest());
            forest.setSummoningSick(false);
        }
        HootingMandrills mandrills = new HootingMandrills();
        testHarness.setHand(testAiPlayer, List.of(mandrills));
        testHarness.setGraveyard(testAiPlayer, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        testHarness.forceActivePlayer(testAiPlayer);
        testHarness.forceStep(TurnStep.PRECOMBAT_MAIN);
        testHarness.clearPriorityPassed();
        testGameData.status = GameStatus.RUNNING;
        testGameData.interaction.clearAwaitingInput();
        testGameData.stack.clear();

        testAi.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(testGameData.stack).hasSize(1);
        assertThat(testGameData.stack.getFirst().getCard()).isSameAs(mandrills);
        assertThat(testGameData.playerGraveyards.get(testAiPlayer.getId())).hasSize(3);
        assertThat(testGameData.getPlayerExiledCards(testAiPlayer.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Easy AI pays a battlefield-imposed sacrifice tax when casting a black spell")
    void castsBlackSpellWithDroughtTax() {
        GameTestHarness testHarness = new GameTestHarness();
        Player testAiPlayer = testHarness.getPlayer2();
        Player opponent = testHarness.getPlayer1();
        GameData testGameData = testHarness.getGameData();
        testHarness.skipMulligan();

        testHarness.addToBattlefield(opponent, new Drought());
        Permanent swamp = testHarness.addToBattlefieldAndReturn(testAiPlayer, new Swamp());
        DauthiMercenary mercenary = new DauthiMercenary();
        testHarness.setHand(testAiPlayer, List.of(mercenary));
        testHarness.addMana(testAiPlayer, ManaColor.BLACK, 1);
        testHarness.addMana(testAiPlayer, ManaColor.COLORLESS, 2);
        testHarness.forceActivePlayer(testAiPlayer);
        testHarness.forceStep(TurnStep.PRECOMBAT_MAIN);
        testHarness.clearPriorityPassed();
        testGameData.status = GameStatus.RUNNING;
        testGameData.interaction.clearAwaitingInput();
        testGameData.stack.clear();

        EasyAiDecisionEngine testAi = new EasyAiDecisionEngine(
                testGameData.id, testAiPlayer, testHarness.getGameRegistry(), testHarness.getGameService(),
                testHarness.getGameQueryService(), testHarness.getBlockLegalityService(),
                testHarness.getCombatAttackService(), testHarness.getGameActionAvailabilityService(),
                testHarness.getCastingCostService(), testHarness.getCastingPermissionService(),
                testHarness.getTargetValidationService(), testHarness.getTargetLegalityService());

        testAi.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(testGameData.stack).hasSize(1);
        assertThat(testGameData.stack.getFirst().getCard()).isSameAs(mercenary);
        assertThat(testGameData.playerBattlefields.get(testAiPlayer.getId())).doesNotContain(swamp);
    }

    @Test
    @DisplayName("Easy AI taps enough mana to pay a colored spell-cost increase")
    void paysColoredSpellCostIncrease() {
        GameTestHarness testHarness = new GameTestHarness();
        Player testAiPlayer = testHarness.getPlayer2();
        GameData testGameData = testHarness.getGameData();
        testHarness.skipMulligan();

        testHarness.addToBattlefield(testAiPlayer, new Derelor());
        List<Permanent> manaSources = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            manaSources.add(testHarness.addToBattlefieldAndReturn(testAiPlayer, new Swamp()));
        }
        FaithOfTheDevoted faith = new FaithOfTheDevoted();
        testHarness.setHand(testAiPlayer, List.of(faith));
        testHarness.forceActivePlayer(testAiPlayer);
        testHarness.forceStep(TurnStep.PRECOMBAT_MAIN);
        testHarness.clearPriorityPassed();
        testGameData.status = GameStatus.RUNNING;
        testGameData.interaction.clearAwaitingInput();
        testGameData.stack.clear();

        EasyAiDecisionEngine testAi = new EasyAiDecisionEngine(
                testGameData.id, testAiPlayer, testHarness.getGameRegistry(), testHarness.getGameService(),
                testHarness.getGameQueryService(), testHarness.getBlockLegalityService(),
                testHarness.getCombatAttackService(), testHarness.getGameActionAvailabilityService(),
                testHarness.getCastingCostService(), testHarness.getCastingPermissionService(),
                testHarness.getTargetValidationService(), testHarness.getTargetLegalityService());

        testAi.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(testGameData.stack).hasSize(1);
        assertThat(testGameData.stack.getFirst().getCard()).isSameAs(faith);
        assertThat(manaSources).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Easy AI supplies Torgaar's sacrifice-based cost reduction")
    void castsTorgaarWithSacrificeCostReduction() {
        GameTestHarness testHarness = new GameTestHarness();
        Player testAiPlayer = testHarness.getPlayer2();
        GameData testGameData = testHarness.getGameData();
        testHarness.skipMulligan();

        Permanent fodder = testHarness.addToBattlefieldAndReturn(testAiPlayer, new TolarianScholar());
        TorgaarFamineIncarnate torgaar = new TorgaarFamineIncarnate();
        testHarness.setHand(testAiPlayer, List.of(torgaar));
        testHarness.addMana(testAiPlayer, ManaColor.BLACK, 2);
        testHarness.addMana(testAiPlayer, ManaColor.COLORLESS, 4);
        testHarness.forceActivePlayer(testAiPlayer);
        testHarness.forceStep(TurnStep.PRECOMBAT_MAIN);
        testHarness.clearPriorityPassed();
        testGameData.status = GameStatus.RUNNING;
        testGameData.interaction.clearAwaitingInput();
        testGameData.stack.clear();

        FakeConnection aiConnection = new FakeConnection("ai-easy-torgaar-test");
        testHarness.getSessionManager().registerPlayer(
                aiConnection, testAiPlayer.getId(), testAiPlayer.getUsername());
        EasyAiDecisionEngine testAi = new EasyAiDecisionEngine(
                testGameData.id, testAiPlayer, testHarness.getGameRegistry(), testHarness.getGameService(),
                testHarness.getGameQueryService(), testHarness.getBlockLegalityService(),
                testHarness.getCombatAttackService(), testHarness.getGameActionAvailabilityService(),
                testHarness.getCastingCostService(), testHarness.getCastingPermissionService(),
                testHarness.getTargetValidationService(), testHarness.getTargetLegalityService());

        testAi.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(testGameData.stack).hasSize(1);
        assertThat(testGameData.stack.getFirst().getCard()).isSameAs(torgaar);
        assertThat(testGameData.playerBattlefields.get(testAiPlayer.getId())).doesNotContain(fodder);
    }

    @Test
    @DisplayName("Easy AI does not spend a Costly Plunder sacrifice target for mana")
    void doesNotSpendCostlyPlunderSacrificeTargetForMana() {
        GameTestHarness testHarness = new GameTestHarness();
        Player testAiPlayer = testHarness.getPlayer2();
        GameData testGameData = testHarness.getGameData();
        testHarness.skipMulligan();

        Permanent basalThrull = testHarness.addToBattlefieldAndReturn(testAiPlayer, new BasalThrull());
        basalThrull.setSummoningSick(false);
        Permanent swamp = testHarness.addToBattlefieldAndReturn(testAiPlayer, new Swamp());
        swamp.setSummoningSick(false);
        CostlyPlunder costlyPlunder = new CostlyPlunder();
        testHarness.setHand(testAiPlayer, List.of(costlyPlunder));
        testHarness.forceActivePlayer(testAiPlayer);
        testHarness.forceStep(TurnStep.PRECOMBAT_MAIN);
        testHarness.clearPriorityPassed();
        testGameData.status = GameStatus.RUNNING;
        testGameData.interaction.clearAwaitingInput();
        testGameData.stack.clear();

        EasyAiDecisionEngine testAi = new EasyAiDecisionEngine(
                testGameData.id, testAiPlayer, testHarness.getGameRegistry(), testHarness.getGameService(),
                testHarness.getGameQueryService(), testHarness.getBlockLegalityService(),
                testHarness.getCombatAttackService(), testHarness.getGameActionAvailabilityService(),
                testHarness.getCastingCostService(), testHarness.getCastingPermissionService(),
                testHarness.getTargetValidationService(), testHarness.getTargetLegalityService());

        testAi.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(testGameData.stack).isEmpty();
        assertThat(testGameData.playerHands.get(testAiPlayer.getId())).containsExactly(costlyPlunder);
        assertThat(testGameData.playerBattlefields.get(testAiPlayer.getId()))
                .containsExactly(basalThrull, swamp);
        assertThat(basalThrull.isTapped()).isFalse();
        assertThat(swamp.isTapped()).isFalse();
        assertThat(testGameData.playerManaPools.get(testAiPlayer.getId()).getTotal()).isZero();

        for (int i = 0; i < 2; i++) {
            Permanent additionalSwamp = testHarness.addToBattlefieldAndReturn(testAiPlayer, new Swamp());
            additionalSwamp.setSummoningSick(false);
        }
        testHarness.forceActivePlayer(testAiPlayer);
        testHarness.forceStep(TurnStep.PRECOMBAT_MAIN);
        testHarness.clearPriorityPassed();
        testGameData.status = GameStatus.RUNNING;
        testGameData.interaction.clearAwaitingInput();
        testGameData.stack.clear();

        testAi.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(testGameData.stack).hasSize(1);
        assertThat(testGameData.stack.getFirst().getCard()).isSameAs(costlyPlunder);
        assertThat(testGameData.playerHands.get(testAiPlayer.getId())).doesNotContain(costlyPlunder);
        assertThat(testGameData.playerBattlefields.get(testAiPlayer.getId())).doesNotContain(basalThrull);
    }

    @Nested
    @DisplayName("Modal spell targets")
    class ModalSpellTargetTests {

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

            FakeConnection aiConn = new FakeConnection("ai-easy-modal-test");
            testHarness.getSessionManager().registerPlayer(aiConn, aiTestPlayer.getId(), "Bob");
            easyAi = new EasyAiDecisionEngine(testGd.id, aiTestPlayer, testHarness.getGameRegistry(),
                    testHarness.getGameService(), testHarness.getGameQueryService(),
                    testHarness.getBlockLegalityService(), testHarness.getCombatAttackService(), testHarness.getGameActionAvailabilityService(),
                    testHarness.getCastingCostService(), testHarness.getCastingPermissionService(),
                    testHarness.getTargetValidationService(), testHarness.getTargetLegalityService());
        }

        private void giveAiPriority() {
            testHarness.forceActivePlayer(aiTestPlayer);
            testHarness.forceStep(TurnStep.PRECOMBAT_MAIN);
            testHarness.clearPriorityPassed();
            testGd.status = GameStatus.RUNNING;
            testGd.interaction.clearAwaitingInput();
            testGd.stack.clear();
        }

        private void giveManaSources(java.util.function.Supplier<? extends Card> landFactory, int count) {
            for (int i = 0; i < count; i++) {
                Permanent permanent = new Permanent(landFactory.get());
                permanent.setSummoningSick(false);
                testGd.playerBattlefields.get(aiTestPlayer.getId()).add(permanent);
            }
        }

        @Test
        @DisplayName("Easy AI starts Evangelize by choosing the opponent")
        void startsEvangelizeWithOpponentChoice() {
            giveAiPriority();
            giveManaSources(Plains::new, 5);
            testHarness.addToBattlefield(human, new GrizzlyBears());
            Evangelize evangelize = new Evangelize();
            testHarness.setHand(aiTestPlayer, List.of(evangelize));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            PendingInteraction.PermanentChoice choice =
                    testGd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice.playerId()).isEqualTo(aiTestPlayer.getId());
            assertThat(choice.validIds()).containsExactly(human.getId());
            assertThat(testGd.stack).isEmpty();
            assertThat(testGd.playerHands.get(aiTestPlayer.getId())).containsExactly(evangelize);
        }

        @Test
        @DisplayName("Easy AI chooses a creature type for Selective Snare")
        void castsSelectiveSnareWithCreatureTypeChoice() {
            giveAiPriority();
            giveManaSources(Island::new, 3);
            Permanent target = testHarness.addToBattlefieldAndReturn(human, new GrizzlyBears());
            SelectiveSnare snare = new SelectiveSnare();
            testHarness.setHand(aiTestPlayer, List.of(snare));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(snare);
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
            assertThat(testGd.stack.getFirst().getChosenCreatureType()).isEqualTo(CardSubtype.BEAR);

            testHarness.passBothPriorities();

            assertThat(testGd.playerBattlefields.get(human.getId())).doesNotContain(target);
        }

        @Test
        @DisplayName("Easy AI supplies a creature type for an additional cast cost")
        void castsCallerOfTheHuntWithCreatureTypeChoice() {
            giveAiPriority();
            giveManaSources(Forest::new, 3);
            testHarness.addToBattlefield(human, new EliteVanguard());
            CallerOfTheHunt caller = new CallerOfTheHunt();
            testHarness.setHand(aiTestPlayer, List.of(caller));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(caller);
            assertThat(testGd.stack.getFirst().getBeholdChosenSubtype()).isEqualTo(CardSubtype.HUMAN);

            testHarness.passBothPriorities();

            assertThat(testGd.playerBattlefields.get(aiTestPlayer.getId()))
                    .anyMatch(permanent -> permanent.getCard() == caller);
        }

        @Test
        @DisplayName("Easy AI announces Risk Factor's opponent target")
        void castsRiskFactorAtOpponent() {
            giveAiPriority();
            giveManaSources(Mountain::new, 3);
            RiskFactor riskFactor = new RiskFactor();
            testHarness.setHand(aiTestPlayer, List.of(riskFactor));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(riskFactor);
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(human.getId());
        }

        @Test
        @DisplayName("Easy AI limits Phyrexian Purge targets to its available life")
        void limitsPerTargetLifeCostTargets() {
            giveAiPriority();
            testHarness.setLife(aiTestPlayer, 7);
            testHarness.addMana(aiTestPlayer, ManaColor.BLACK, 2);
            testHarness.addMana(aiTestPlayer, ManaColor.RED, 2);
            testHarness.addToBattlefield(human, new SerraAngel());
            testHarness.addToBattlefield(human, new GrizzlyBears());
            testHarness.addToBattlefield(human, new LlanowarElves());
            PhyrexianPurge purge = new PhyrexianPurge();
            testHarness.setHand(aiTestPlayer, List.of(purge));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(purge);
            assertThat(testGd.stack.getFirst().getTargetIds()).hasSize(2);
            assertThat(testGd.getLife(aiTestPlayer.getId())).isEqualTo(1);
        }

        @Test
        @DisplayName("Easy AI announces one target when paying no repeatable additional cost")
        void castsPrimitiveJusticeWithItsBaseTargetCount() {
            giveAiPriority();
            giveManaSources(Mountain::new, 2);
            Permanent artifact = testHarness.addToBattlefieldAndReturn(human, new Ornithopter());
            PrimitiveJustice primitiveJustice = new PrimitiveJustice();
            testHarness.setHand(aiTestPlayer, List.of(primitiveJustice));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(primitiveJustice);
            assertThat(testGd.stack.getFirst().getXValue()).isEqualTo(1);
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(artifact.getId());
            assertThat(testGd.stack.getFirst().getRepeatedAdditionalCosts()).isEmpty();
        }

        @Test
        @DisplayName("Easy AI uses the base X when paying no multikicker payments")
        void castsStrengthOfTheTajuruWithoutMultikicker() {
            giveAiPriority();
            testHarness.addMana(aiTestPlayer, ManaColor.GREEN, 4);
            Permanent creature = testHarness.addToBattlefieldAndReturn(aiTestPlayer, new GrizzlyBears());
            StrengthOfTheTajuru strengthOfTheTajuru = new StrengthOfTheTajuru();
            testHarness.setHand(aiTestPlayer, List.of(strengthOfTheTajuru));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(strengthOfTheTajuru);
            assertThat(testGd.stack.getFirst().getXValue()).isEqualTo(1);
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(creature.getId());
            assertThat(testGd.stack.getFirst().getRepeatedAdditionalCosts()).isEmpty();
        }

        @Test
        @DisplayName("Easy AI does not submit a Dream Halls-only alternate cast as a mana cast")
        void skipsDreamHallsOnlyAlternateCast() {
            giveAiPriority();
            testHarness.addToBattlefield(aiTestPlayer, new DreamHalls());
            giveManaSources(Island::new, 3);
            CurseOfEchoes curseOfEchoes = new CurseOfEchoes();
            AirElemental airElemental = new AirElemental();
            testHarness.setHand(aiTestPlayer, List.of(curseOfEchoes, airElemental));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).isEmpty();
            assertThat(testGd.playerHands.get(aiTestPlayer.getId()))
                    .containsExactly(curseOfEchoes, airElemental);
            assertThat(testGd.playerManaPools.get(aiTestPlayer.getId()).getTotal()).isZero();
            assertThat(testGd.playerBattlefields.get(aiTestPlayer.getId()))
                    .allMatch(permanent -> !permanent.isTapped());
        }

        @Test
        @DisplayName("Easy AI casts Cryptic Command with its choose-two target")
        void castsCrypticCommandWithChooseTwoTarget() {
            giveAiPriority();
            giveManaSources(Island::new, 4);
            Permanent target = new Permanent(new GrizzlyBears());
            testGd.playerBattlefields.get(human.getId()).add(target);
            testHarness.setHand(aiTestPlayer, List.of(new CrypticCommand()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard().getName()).isEqualTo("Cryptic Command");
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
        }

        @Test
        @DisplayName("Easy AI supplies targets for Ashling's Command's targeted modes")
        void castsAshlingsCommandWithTargetsForEachChosenMode() {
            giveAiPriority();
            giveManaSources(Island::new, 4);
            giveManaSources(Mountain::new, 1);
            Permanent elemental = testHarness.addToBattlefieldAndReturn(aiTestPlayer, new AirElemental());
            AshlingsCommand command = new AshlingsCommand();
            testHarness.setHand(aiTestPlayer, List.of(command));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getTargetId()).isNull();
            assertThat(testGd.stack.getFirst().getTargetIds())
                    .containsExactly(elemental.getId(), human.getId());
        }

        @Test
        @DisplayName("Easy AI validates modal player-or-planeswalker target candidates")
        void castsModalPlayerOrPlaneswalkerSpellAtPlayer() {
            giveAiPriority();
            giveManaSources(Mountain::new, 1);
            giveManaSources(Plains::new, 1);
            Permanent ordinaryPermanent = testHarness.addToBattlefieldAndReturn(human, new GrizzlyBears());
            Card modalSpell = new Card();
            modalSpell.setName("Draw and Bolt");
            modalSpell.setType(CardType.SORCERY);
            modalSpell.setManaCost("{R}{W}");
            modalSpell.addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                    new ChooseOneEffect.ChooseOneOption("Draw and damage", List.of(
                            new DrawCardEffect(1), new DealDamageToTargetPlayerOrPlaneswalkerEffect(1))),
                    new ChooseOneEffect.ChooseOneOption("Draw", new DrawCardEffect(1)))));
            testHarness.setHand(aiTestPlayer, List.of(modalSpell));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(human.getId());
            assertThat(testGd.stack.getFirst().getTargetId()).isNotEqualTo(ordinaryPermanent.getId());
        }

        @Test
        @DisplayName("Easy AI chooses Wear // Tear's affordable mode")
        void choosesAffordableWearTearMode() {
            giveAiPriority();
            giveManaSources(Plains::new, 1);
            Permanent artifact = testHarness.addToBattlefieldAndReturn(human, new Ornithopter());
            Permanent enchantment = testHarness.addToBattlefieldAndReturn(human, new AngelicChorus());
            testHarness.setHand(aiTestPlayer, List.of(new WearTear()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard().getName()).isEqualTo("Wear");
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(enchantment.getId());
            assertThat(testGd.playerBattlefields.get(aiTestPlayer.getId()))
                    .filteredOn(Permanent::isTapped)
                    .hasSize(1);
            assertThat(testGd.playerManaPools.get(aiTestPlayer.getId()).get(ManaColor.RED)).isZero();
            assertThat(testGd.playerManaPools.get(aiTestPlayer.getId()).get(ManaColor.WHITE)).isZero();
            assertThat(testGd.playerBattlefields.get(human.getId()))
                    .extracting(permanent -> permanent.getCard().getName())
                    .containsExactly(artifact.getCard().getName(), enchantment.getCard().getName());
        }

        @Test
        @DisplayName("Easy AI pays the selected spree mode's additional mana cost")
        void castsSpreeModeWithItsAdditionalManaCost() {
            giveAiPriority();
            giveManaSources(Plains::new, 2);
            FinalShowdown finalShowdown = new FinalShowdown();
            testHarness.setHand(aiTestPlayer, List.of(finalShowdown));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard().getId()).isEqualTo(finalShowdown.getId());
            assertThat(testGd.playerBattlefields.get(aiTestPlayer.getId()))
                    .filteredOn(Permanent::isTapped)
                    .hasSize(2);
        }

        @Test
        @DisplayName("Easy AI casts a targetless modal double-faced card face")
        void castsTargetlessModalDoubleFacedCardFace() {
            giveAiPriority();
            giveManaSources(Mountain::new, 4);
            ToralfGodOfFury toralf = new ToralfGodOfFury();
            testHarness.setHand(aiTestPlayer, List.of(toralf));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard().getId()).isEqualTo(toralf.getId());
            assertThat(testGd.stack.getFirst().getXValue()).isZero();
        }

        @Test
        @DisplayName("Easy AI supplies a matching permanent for a behold additional cost")
        void castsBeholdSpellWithMatchingPermanent() {
            giveAiPriority();
            giveManaSources(Mountain::new, 4);
            Permanent elemental = testHarness.addToBattlefieldAndReturn(aiTestPlayer, new AirElemental());
            elemental.setSummoningSick(false);
            ChampionOfThePath champion = new ChampionOfThePath();
            testHarness.setHand(aiTestPlayer, List.of(champion));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(champion);
            assertThat(testGd.getPlayerExiledCards(aiTestPlayer.getId()))
                    .extracting(Card::getId)
                    .contains(elemental.getCard().getId());
        }

        @Test
        @DisplayName("Easy AI supplies both targets for Blinding Beam's tap mode")
        void castsBlindingBeamWithTwoTargetCreatures() {
            giveAiPriority();
            giveManaSources(Plains::new, 3);
            Permanent firstTarget = new Permanent(new GrizzlyBears());
            Permanent secondTarget = new Permanent(new GrizzlyBears());
            testGd.playerBattlefields.get(human.getId()).add(firstTarget);
            testGd.playerBattlefields.get(human.getId()).add(secondTarget);
            testHarness.setHand(aiTestPlayer, List.of(new BlindingBeam()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard().getName()).isEqualTo("Blinding Beam");
            assertThat(testGd.stack.getFirst().getTargetId()).isNull();
            assertThat(testGd.stack.getFirst().getTargetIds())
                    .containsExactlyInAnyOrder(firstTarget.getId(), secondTarget.getId());
        }

        @Test
        @DisplayName("Easy AI collects evidence for Urgent Necropsy's targets")
        void castsUrgentNecropsyWithTargetEvidence() {
            giveAiPriority();
            testHarness.addMana(aiTestPlayer, ManaColor.BLACK, 1);
            testHarness.addMana(aiTestPlayer, ManaColor.GREEN, 1);
            testHarness.addMana(aiTestPlayer, ManaColor.COLORLESS, 2);
            Permanent target = testHarness.addToBattlefieldAndReturn(human, new GrizzlyBears());
            GrizzlyBears evidence = new GrizzlyBears();
            testHarness.setGraveyard(aiTestPlayer, List.of(evidence));
            UrgentNecropsy necropsy = new UrgentNecropsy();
            testHarness.setHand(aiTestPlayer, List.of(necropsy));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(necropsy);
            assertThat(testGd.stack.getFirst().getTargetIds()).containsExactly(target.getId());
            assertThat(testGd.getPlayerExiledCards(aiTestPlayer.getId())).containsExactly(evidence);
        }

        @Test
        @DisplayName("Easy AI allows one permanent for both Reign of Chaos targets")
        void castsReignOfChaosWithSharedTarget() {
            giveAiPriority();
            giveManaSources(Mountain::new, 4);
            Permanent target = new Permanent(whitePlainsCreature());
            testGd.playerBattlefields.get(human.getId()).add(target);
            testHarness.setHand(aiTestPlayer, List.of(new ReignOfChaos()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard().getName()).isEqualTo("Reign of Chaos");
            assertThat(testGd.stack.getFirst().getTargetId()).isNull();
            assertThat(testGd.stack.getFirst().getTargetIds())
                    .containsExactly(target.getId(), target.getId());
        }

        @Test
        @DisplayName("Easy AI sends variable-count modal targets through target slots")
        void castsVariableCountModalSpellWithTargetSlot() {
            giveAiPriority();
            giveManaSources(Mountain::new, 1);
            Permanent target = new Permanent(new GrizzlyBears());
            testGd.playerBattlefields.get(human.getId()).add(target);
            testHarness.setHand(aiTestPlayer, List.of(new BorrowedHostility()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard().getName()).isEqualTo("Borrowed Hostility");
            assertThat(testGd.stack.getFirst().getTargetId()).isNull();
            assertThat(testGd.stack.getFirst().getTargetIds()).containsExactly(target.getId());
        }

        @Test
        @DisplayName("Easy AI casts Pyrrhic Strike's single mode without paying blight")
        void castsSingleModeWithoutPayingOptionalBlight() {
            giveAiPriority();
            giveManaSources(Plains::new, 3);
            Permanent blightCreature = testHarness.addToBattlefieldAndReturn(aiTestPlayer, new HillGiant());
            Permanent artifact = testHarness.addToBattlefieldAndReturn(human, new Ornithopter());
            testHarness.setHand(aiTestPlayer, List.of(new PyrrhicStrike()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getTargetIds()).containsExactly(artifact.getId());
            assertThat(blightCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        }

        @Test
        @DisplayName("Easy AI pays an X-discard additional cost")
        void castsAbandonHopeWithRequiredDiscardCard() {
            giveAiPriority();
            giveManaSources(Swamp::new, 3);
            AbandonHope abandonHope = new AbandonHope();
            GrizzlyBears discard = new GrizzlyBears();
            testHarness.setHand(aiTestPlayer, List.of(abandonHope, discard));
            testHarness.setHand(human, List.of(new GrizzlyBears()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(abandonHope);
            assertThat(testGd.stack.getFirst().getXValue()).isEqualTo(1);
            assertThat(testGd.playerHands.get(aiTestPlayer.getId())).isEmpty();
            assertThat(testGd.playerGraveyards.get(aiTestPlayer.getId()))
                    .containsExactly(discard);
        }

        @Test
        @DisplayName("Easy AI supplies all cards for a fixed multi-card discard cost")
        void castsCatharticReunionWithTwoDiscardCards() {
            giveAiPriority();
            giveManaSources(Mountain::new, 2);
            CatharticReunion reunion = new CatharticReunion();
            GrizzlyBears firstDiscard = new GrizzlyBears();
            GrizzlyBears secondDiscard = new GrizzlyBears();
            GrizzlyBears remainingCard = new GrizzlyBears();
            testHarness.setHand(aiTestPlayer, List.of(reunion, firstDiscard, secondDiscard, remainingCard));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(reunion);
            assertThat(testGd.playerGraveyards.get(aiTestPlayer.getId()))
                    .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
            assertThat(testGd.playerHands.get(aiTestPlayer.getId())).containsExactly(remainingCard);
        }

        @Test
        @DisplayName("Easy AI chooses only discard for an either-or additional cost")
        void castsEitherOrCostSpellByDiscardingWithoutSacrificing() {
            giveAiPriority();
            testHarness.addMana(aiTestPlayer, ManaColor.RED, 1);
            testHarness.addMana(aiTestPlayer, ManaColor.COLORLESS, 1);
            Permanent artifact = testHarness.addToBattlefieldAndReturn(aiTestPlayer, new HowlingMine());
            Card spell = eitherOrCostCreature();
            GrizzlyBears discardedCard = new GrizzlyBears();
            testHarness.setHand(aiTestPlayer, List.of(spell, discardedCard));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(spell);
            assertThat(testGd.playerGraveyards.get(aiTestPlayer.getId())).containsExactly(discardedCard);
            assertThat(testGd.playerBattlefields.get(aiTestPlayer.getId())).contains(artifact);
        }

        private Card eitherOrCostCreature() {
            Card card = new Card();
            card.setName("Either-Or Cost Creature");
            card.setType(CardType.CREATURE);
            card.setManaCost("{1}{R}");
            card.setPower(2);
            card.setToughness(2);
            card.addEffect(EffectSlot.SPELL,
                    new SacrificePermanentOrDiscardCardCost(new PermanentIsArtifactPredicate(), "an artifact"));
            return card;
        }

        @Test
        @DisplayName("Easy AI supplies untapped creatures for convoke")
        void castsConvokeSpellWithUntappedCreatures() {
            giveAiPriority();
            giveManaSources(Island::new, 2);
            Permanent firstCreature = testHarness.addToBattlefieldAndReturn(aiTestPlayer, new GrizzlyBears());
            Permanent secondCreature = testHarness.addToBattlefieldAndReturn(aiTestPlayer, new GrizzlyBears());
            firstCreature.setSummoningSick(false);
            secondCreature.setSummoningSick(false);

            Card convokeSpell = new Card();
            convokeSpell.setName("Convoke Test Creature");
            convokeSpell.setType(CardType.CREATURE);
            convokeSpell.setManaCost("{3}{U}");
            convokeSpell.setPower(4);
            convokeSpell.setToughness(4);
            convokeSpell.setKeywords(EnumSet.of(Keyword.CONVOKE));
            testHarness.setHand(aiTestPlayer, List.of(convokeSpell));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(convokeSpell);
            assertThat(firstCreature.isTapped()).isTrue();
            assertThat(secondCreature.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Easy AI does not cast Unbury when neither mode has a legal graveyard target")
        void doesNotCastUnburyWithoutCreatureInGraveyard() {
            giveAiPriority();
            giveManaSources(Swamp::new, 2);
            testGd.playerGraveyards.get(aiTestPlayer.getId()).add(new HolyDay());
            testHarness.setHand(aiTestPlayer, List.of(new Unbury()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).isEmpty();
            assertThat(testGd.playerBattlefields.get(aiTestPlayer.getId()))
                    .allMatch(permanent -> !permanent.isTapped());
            assertThat(testGd.playerHands.get(aiTestPlayer.getId())).singleElement()
                    .isInstanceOf(Unbury.class);
        }

        @Test
        @DisplayName("Easy AI does not cast Victimize without two creature cards in its graveyard")
        void doesNotCastVictimizeWithoutTwoCreatureCardsInGraveyard() {
            giveAiPriority();
            giveManaSources(Swamp::new, 3);
            testGd.playerGraveyards.get(aiTestPlayer.getId()).add(new HolyDay());
            Victimize victimize = new Victimize();
            testHarness.setHand(aiTestPlayer, List.of(victimize));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).isEmpty();
            assertThat(testGd.playerBattlefields.get(aiTestPlayer.getId()))
                    .allMatch(permanent -> !permanent.isTapped());
            assertThat(testGd.playerHands.get(aiTestPlayer.getId())).containsExactly(victimize);
        }

        @Test
        @DisplayName("Easy AI does not cast Suffer the Past without cards in the target graveyard")
        void doesNotCastSufferThePastWithoutCardsInTargetGraveyard() {
            giveAiPriority();
            giveManaSources(Swamp::new, 2);
            SufferThePast sufferThePast = new SufferThePast();
            testHarness.setHand(aiTestPlayer, List.of(sufferThePast));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).isEmpty();
            assertThat(testGd.playerBattlefields.get(aiTestPlayer.getId()))
                    .allMatch(permanent -> !permanent.isTapped());
            assertThat(testGd.playerHands.get(aiTestPlayer.getId())).containsExactly(sufferThePast);
        }

        @Test
        @DisplayName("Easy AI keeps Torrent of Souls' optional graveyard target separate from its player target")
        void castsTorrentOfSoulsWithoutOptionalGraveyardTarget() {
            giveAiPriority();
            giveManaSources(Swamp::new, 2);
            giveManaSources(Mountain::new, 3);
            TorrentOfSouls torrentOfSouls = new TorrentOfSouls();
            testHarness.setHand(aiTestPlayer, List.of(torrentOfSouls));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(torrentOfSouls);
            assertThat(testGd.stack.getFirst().getTargetId()).isNull();
            assertThat(testGd.stack.getFirst().getTargetIds()).containsExactly(human.getId());
        }

        @Test
        @DisplayName("Easy AI limits Finale of Promise graveyard targets to the announced X")
        void castsFinaleOfPromiseWithAffordableGraveyardTargets() {
            giveAiPriority();
            giveManaSources(Mountain::new, 4);
            Opportunity tooExpensive = new Opportunity();
            Shock affordable = new Shock();
            testHarness.setGraveyard(aiTestPlayer, List.of(tooExpensive, affordable));
            FinaleOfPromise finale = new FinaleOfPromise();
            testHarness.setHand(aiTestPlayer, List.of(finale));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(finale);
            assertThat(testGd.stack.getFirst().getXValue()).isEqualTo(2);
            assertThat(testGd.stack.getFirst().getTargetId()).isNull();
            assertThat(testGd.stack.getFirst().getTargetIds()).containsExactly(affordable.getId());
        }

        @Test
        @DisplayName("Easy AI does not cast Ajani's Response at an unaffordable untapped target")
        void doesNotCastTargetReducedSpellAtUnaffordableTarget() {
            giveAiPriority();
            giveManaSources(Plains::new, 1);
            giveManaSources(Island::new, 2);

            Permanent ownTappedCreature = testHarness.addToBattlefieldAndReturn(aiTestPlayer, new GrizzlyBears());
            ownTappedCreature.setSummoningSick(false);
            ownTappedCreature.tap();
            Permanent opponentCreature = testHarness.addToBattlefieldAndReturn(human, new GrizzlyBears());
            opponentCreature.setSummoningSick(false);
            testHarness.setHand(aiTestPlayer, List.of(new AjanisResponse()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).isEmpty();
            assertThat(testGd.playerBattlefields.get(aiTestPlayer.getId()))
                    .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                    .allMatch(permanent -> !permanent.isTapped());
        }
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

        // Only land mana available A?€�t no creature mana
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.COLORLESS, 2);

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT attempt to cast A?€�t creature mana requirement not met
        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler).handlePassPriority(any());
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

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        // Should attempt to cast A?€�t creature mana requirement met
        verify(messageHandler).handlePlayCard(any());
    }

    // ===== Sacrifice cost restriction =====

    @Test
    @DisplayName("Easy AI does not attempt to cast spell with a sacrifice-an-artifact cost when no artifact available")
    void doesNotCastSacrificeArtifactCostWithNoArtifact() throws Exception {
        Card sacrificeSpell = new Card();
        sacrificeSpell.setName("Test Artifact Sac");
        sacrificeSpell.setType(CardType.SORCERY);
        sacrificeSpell.setManaCost("{R}");
        sacrificeSpell.addEffect(EffectSlot.SPELL,
                new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false));
        gd.playerHands.get(aiPlayer.getId()).add(sacrificeSpell);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT attempt to cast A?€�t no artifact to sacrifice
        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler).handlePassPriority(any());
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

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT attempt to cast A?€�t no creature to sacrifice
        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler).handlePassPriority(any());
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
        }).when(messageHandler).handlePlayCard(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(captor.capture());

        PlayCardRequest request = captor.getValue();
        assertThat(request.sacrificePermanentId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Easy AI passes sacrificePermanentId for a put-counter additional cast cost")
    void passesSacrificePermanentIdForPutCounterCost() throws Exception {
        Card counterSpell = new Card();
        counterSpell.setName("Test Counter Cost Spell");
        counterSpell.setType(CardType.SORCERY);
        counterSpell.setManaCost("{R}");
        counterSpell.addEffect(EffectSlot.SPELL,
                new PutCounterOnControlledCreatureCost(CounterType.MINUS_ONE_MINUS_ONE, 1));
        gd.playerHands.get(aiPlayer.getId()).add(counterSpell);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);

        Card creatureCard = new Card();
        creatureCard.setName("Counter Bearer");
        creatureCard.setType(CardType.CREATURE);
        creatureCard.setPower(2);
        creatureCard.setToughness(2);
        Permanent creature = new Permanent(creatureCard);
        gd.playerBattlefields.get(aiPlayer.getId()).add(creature);

        when(gameQueryService.isCreature(gd, creature)).thenReturn(true);

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(captor.capture());

        assertThat(captor.getValue().sacrificePermanentId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Easy AI passes sacrificePermanentId for a return-to-hand additional cast cost")
    void passesSacrificePermanentIdForReturnCreatureToHandCost() throws Exception {
        Card bounceSpell = new Card();
        bounceSpell.setName("Test Bounce Cost Spell");
        bounceSpell.setType(CardType.SORCERY);
        bounceSpell.setManaCost("{R}");
        bounceSpell.addEffect(EffectSlot.SPELL, new ReturnCreatureToHandCost());
        gd.playerHands.get(aiPlayer.getId()).add(bounceSpell);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);

        Card creatureCard = new Card();
        creatureCard.setName("Bounce Fodder");
        creatureCard.setType(CardType.CREATURE);
        creatureCard.setPower(1);
        creatureCard.setToughness(1);
        Permanent creature = new Permanent(creatureCard);
        gd.playerBattlefields.get(aiPlayer.getId()).add(creature);

        when(gameQueryService.isCreature(gd, creature)).thenReturn(true);

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(captor.capture());

        assertThat(captor.getValue().sacrificePermanentId()).isEqualTo(creature.getId());
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
        }).when(messageHandler).handlePlayCard(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(captor.capture());

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

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        verify(messageHandler).handlePlayCard(any());
        verify(messageHandler).handlePassPriority(any());
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
        }).when(messageHandler).handlePlayCard(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        verify(messageHandler).handlePlayCard(any());
        verify(messageHandler, never()).handlePassPriority(any());
    }

    // ===== Identity-based cast detection (explore-refill regression) =====

    @Test
    @DisplayName("Easy AI detects cast success when ETB refills hand with a land (e.g. Explore)")
    void detectsCastSuccessWhenEtbRefillsHandWithLand() throws Exception {
        // Regression: Queen's Agent ETB triggers Explore which can refill hand with a land,
        // leaving hand.size() unchanged. The fix uses identity (hand.contains) not size.
        Card creature = new Card();
        creature.setName("Queen's Agent");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{5}{B}");
        creature.setPower(3);
        creature.setToughness(3);
        gd.playerHands.get(aiPlayer.getId()).add(creature);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.BLACK, 1);
        pool.add(ManaColor.COLORLESS, 5);

        Mockito.doAnswer(inv -> {
            // Simulate explore: remove the creature from hand, add a land (null mana cost)
            List<Card> hand = gd.playerHands.get(aiPlayer.getId());
            hand.remove(creature);
            Card revealedLand = new Card();
            revealedLand.setName("Forest");
            revealedLand.setType(CardType.LAND);
            // Lands have null manaCost A?€�t this is what triggered the downstream NPE
            hand.add(revealedLand);
            return null;
        }).when(messageHandler).handlePlayCard(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        verify(messageHandler).handlePlayCard(any());
        // Cast succeeded (creature is no longer in hand) A?€�t AI must NOT pass priority
        verify(messageHandler, never()).handlePassPriority(any());
    }

    @Test
    @DisplayName("Easy AI still detects genuine silent failure when hand has other cards")
    void detectsGenuineFailureWhenHandHasOtherCards() throws Exception {
        // Hand has two cards A?€�t the castable creature plus a sibling.
        // Simulate a silent failure (handlePlayCard does nothing). Size-based detection
        // would also work here, but identity detection must still see the creature in hand.
        Card creature = new Card();
        creature.setName("Test Bear");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{1}{G}");
        creature.setPower(2);
        creature.setToughness(2);
        Card sibling = new Card();
        sibling.setName("Other Card");
        sibling.setType(CardType.SORCERY);
        sibling.setManaCost("{10}{U}{U}"); // Unaffordable A?€�t AI won't pick it
        gd.playerHands.get(aiPlayer.getId()).add(creature);
        gd.playerHands.get(aiPlayer.getId()).add(sibling);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.GREEN, 1);
        pool.add(ManaColor.COLORLESS, 1);

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        verify(messageHandler).handlePlayCard(any());
        verify(messageHandler).handlePassPriority(any());
    }

    @Test
    @DisplayName("Easy AI does not throw when ETB refills hand with a null-cost land")
    void noExceptionWhenEtbRefillsHandWithNullCostLand() throws Exception {
        // Guards against NPE: before the fix, the stale-hand detection returned "failed",
        // leading downstream code paths to operate on a now-land card with null mana cost.
        Card creature = new Card();
        creature.setName("Queen's Agent");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{5}{B}");
        creature.setPower(3);
        creature.setToughness(3);
        gd.playerHands.get(aiPlayer.getId()).add(creature);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.BLACK, 1);
        pool.add(ManaColor.COLORLESS, 5);

        Mockito.doAnswer(inv -> {
            List<Card> hand = gd.playerHands.get(aiPlayer.getId());
            hand.remove(creature);
            Card revealedLand = new Card();
            revealedLand.setName("Forest");
            revealedLand.setType(CardType.LAND);
            hand.add(revealedLand);
            return null;
        }).when(messageHandler).handlePlayCard(any());

        assertThatCode(() -> createEngine().handleEvent(AiDecisionKind.GAME_STATE))
                .doesNotThrowAnyException();
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

        // block legality returns false for the restricted creature
        when(blockLegalityService.canBlock(nullable(BlockLegalityContext.class), eq(cantBlocker))).thenReturn(false);

        gd.interaction.beginInteraction(new PendingInteraction.BlockerDeclaration(aiPlayer.getId()));
        createEngine().handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

        // Should declare no blockers since the only creature can't block
        ArgumentCaptor<DeclareBlockersRequest> captor = ArgumentCaptor.forClass(DeclareBlockersRequest.class);
        verify(messageHandler).handleDeclareBlockers(captor.capture());
        assertThat(captor.getValue().blockerAssignments()).isEmpty();
    }

    @Test
    @DisplayName("Easy AI ignores a blocker-declaration event for another player")
    void ignoresBlockerDeclarationForAnotherPlayer() {
        UUID opponentId = gd.orderedPlayerIds.get(1);
        gd.interaction.beginInteraction(new PendingInteraction.BlockerDeclaration(opponentId));

        createEngine().handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

        verify(messageHandler, never()).handleDeclareBlockers(any());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class).decidingPlayerId())
                .isEqualTo(opponentId);
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

        // Cost modifier adds 1 (e.g. opponent has Thalia) A?€�t now needs 3 total
        when(castingCostService.getCastCostModifier(any(), any(), any())).thenReturn(1);

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT attempt to cast A?€�t can't afford with cost increase
        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler).handlePassPriority(any());
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

        // Only 3 mana available A?€�t normally can't afford {3}{G} (4 total)
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.GREEN, 1);
        pool.add(ManaColor.COLORLESS, 2);

        // Cost reduction of 1 A?€�t now only needs 3 total
        when(castingCostService.getCastCostModifier(any(), any(), any())).thenReturn(-1);

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        // Should attempt to cast A?€�t affordable with cost reduction
        verify(messageHandler).handlePlayCard(any());
    }

    @Test
    @DisplayName("Easy AI does not cast spell when the engine playability check returns false")
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

        // Engine says not playable (e.g. spell limit reached, type restricted, silenced)
        AiTestPlayabilityStub.installPotentialManaService(actionAvailabilityService, gameQueryService);
        when(actionAvailabilityService.isCardPlayable(any(), any(), any(), any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(false);

        EasyAiDecisionEngine engine = new EasyAiDecisionEngine(
                gd.id, aiPlayer, gameRegistry, messageHandler,
                gameQueryService, blockLegalityService, combatAttackService, actionAvailabilityService,
                castingCostService, castingPermissionService,
                targetValidationService,
                new com.github.laxika.magicalvibes.service.target.TargetLegalityService(gameQueryService,
                        new com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService(gameQueryService),
                        targetValidationService,
                        Mockito.mock(com.github.laxika.magicalvibes.service.effect.AmountEvaluationService.class),
                        new com.github.laxika.magicalvibes.service.target.TargetGroupAssignmentService(gameQueryService)));
        engine.handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT attempt to cast A?€�t spell casting restricted
        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler).handlePassPriority(any());
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
                .addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(3));
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
        }).when(messageHandler).handlePlayCard(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(captor.capture());

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
                .addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(3));
        gd.playerHands.get(aiPlayer.getId()).add(spell);

        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.RED, 1);
        pool.add(ManaColor.COLORLESS, 1);

        // No creatures on opponent's battlefield

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler).handlePassPriority(any());
    }

    @Test
    @DisplayName("Easy AI distributes divided damage among multiple creatures to maximize kills")
    void distributesDividedDamageToMaximizeKills() throws Exception {
        Card spell = new Card();
        spell.setName("Test Divided Damage");
        spell.setType(CardType.SORCERY);
        spell.setManaCost("{1}{R}");
        spell.target(null, 1, 3)
                .addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(3));
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
        }).when(messageHandler).handlePlayCard(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(captor.capture());

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

        // 4 black mana A?†’ without modifier maxX=2, with modifier +1 maxX=1
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.BLACK, 4);

        // Cost modifier +1 (e.g. Thalia on opponent's battlefield)
        when(castingCostService.getCastCostModifier(any(), any(), any())).thenReturn(1);

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(captor.capture());

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

        // 4 black mana A?†’ without modifier maxX=2, with modifier +2 maxX=0
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.BLACK, 4);

        // Cost modifier +2 A?€�t no X value is affordable
        when(castingCostService.getCastCostModifier(any(), any(), any())).thenReturn(2);

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT attempt to cast A?€�t maxX is 0
        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler).handlePassPriority(any());
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

        // 4 black mana A?†’ maxX=2
        ManaPool pool = gd.playerManaPools.get(aiPlayer.getId());
        pool.add(ManaColor.BLACK, 4);

        // No cost modifier
        when(castingCostService.getCastCostModifier(any(), any(), any())).thenReturn(0);

        Mockito.doAnswer(inv -> {
            gd.playerHands.get(aiPlayer.getId()).removeFirst();
            return null;
        }).when(messageHandler).handlePlayCard(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(captor.capture());

        PlayCardRequest request = captor.getValue();
        // maxX should be 2, smartX clamps to target toughness but no target A?†’ full maxX
        assertThat(request.xValue()).isEqualTo(2);
    }

    @Nested
    @DisplayName("Attacker declaration regressions")
    class AttackerDeclarationRegressionTests {

        private GameTestHarness combatHarness;
        private Player opponent;
        private Player combatAiPlayer;
        private GameData combatGd;
        private EasyAiDecisionEngine combatAi;

        @BeforeEach
        void setUpCombatHarness() {
            combatHarness = new GameTestHarness();
            opponent = combatHarness.getPlayer1();
            combatAiPlayer = combatHarness.getPlayer2();
            combatGd = combatHarness.getGameData();
            combatHarness.skipMulligan();
            combatHarness.clearMessages();
            FakeConnection aiConn = new FakeConnection("ai-easy-attacker-regression-test");
            combatHarness.getSessionManager().registerPlayer(aiConn, combatAiPlayer.getId(), "Bob");
            combatAi = new EasyAiDecisionEngine(combatGd.id, combatAiPlayer, combatHarness.getGameRegistry(),
                    combatHarness.getGameService(), combatHarness.getGameQueryService(),
                    combatHarness.getBlockLegalityService(), combatHarness.getCombatAttackService(),
                    combatHarness.getGameActionAvailabilityService(), combatHarness.getCastingCostService(),
                    combatHarness.getCastingPermissionService(), combatHarness.getTargetValidationService(),
                    combatHarness.getTargetLegalityService());
        }

        @Test
        @DisplayName("Easy AI declares a blocker required to block if able")
        void honorsMustBlockIfAbleRequirement() {
            Permanent attacker = combatHarness.addToBattlefieldAndReturn(opponent, new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            Permanent blocker = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            blocker.setSummoningSick(false);
            blocker.setMustBlockThisTurnIfAble(true);

            combatHarness.forceActivePlayer(opponent);
            combatHarness.forceStep(TurnStep.DECLARE_BLOCKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.beginBlockerDeclarationInput();

            combatAi.handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

            assertThat(blocker.isBlocking()).isTrue();
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class))
                    .isNull();
        }

        @Test
        @DisplayName("Easy AI caps Lure blocks to an aura-granted maximum")
        void capsLureBlocksToAuraGrantedMaximum() {
            Permanent attacker = combatHarness.addToBattlefieldAndReturn(opponent, new KjeldoranRoyalGuard());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            Permanent authority = combatHarness.addToBattlefieldAndReturn(opponent, new AlphaAuthority());
            authority.setAttachedTo(attacker.getId());
            Permanent lure = combatHarness.addToBattlefieldAndReturn(opponent, new Lure());
            lure.setAttachedTo(attacker.getId());
            Permanent firstBlocker = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            firstBlocker.setSummoningSick(false);
            Permanent secondBlocker = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            secondBlocker.setSummoningSick(false);

            combatHarness.forceActivePlayer(opponent);
            combatHarness.forceStep(TurnStep.DECLARE_BLOCKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.beginBlockerDeclarationInput();

            int attackerIndex = combatGd.playerBattlefields.get(opponent.getId()).indexOf(attacker);
            int firstBlockerIndex = combatGd.playerBattlefields.get(combatAiPlayer.getId()).indexOf(firstBlocker);
            int secondBlockerIndex = combatGd.playerBattlefields.get(combatAiPlayer.getId()).indexOf(secondBlocker);
            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                combatAi.sendBlockerDeclaration(new DeclareBlockersRequest(List.of(
                        new BlockerAssignment(firstBlockerIndex, attackerIndex),
                        new BlockerAssignment(secondBlockerIndex, attackerIndex))));
                assertThat(watcher.drainFailures()).isEmpty();
            } finally {
                watcher.uninstall();
            }

            assertThat(List.of(firstBlocker, secondBlocker)).filteredOn(Permanent::isBlocking).hasSize(1);
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class))
                    .isNull();
        }

        @Test
        @DisplayName("Easy AI drops a partial Tromokratis block without rejection")
        void dropsPartialTromokratisBlockWithoutRejection() {
            Permanent attacker = combatHarness.addToBattlefieldAndReturn(opponent, new Tromokratis());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            Permanent firstBlocker = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            firstBlocker.setSummoningSick(false);
            Permanent secondBlocker = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new HillGiant());
            secondBlocker.setSummoningSick(false);

            combatHarness.forceActivePlayer(opponent);
            combatHarness.forceStep(TurnStep.DECLARE_BLOCKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.beginBlockerDeclarationInput();

            int firstBlockerIndex = combatGd.playerBattlefields.get(combatAiPlayer.getId()).indexOf(firstBlocker);
            int attackerIndex = combatGd.playerBattlefields.get(opponent.getId()).indexOf(attacker);
            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                combatAi.sendBlockerDeclaration(new DeclareBlockersRequest(List.of(
                        new BlockerAssignment(firstBlockerIndex, attackerIndex))));
                assertThat(watcher.drainFailures()).isEmpty();
            } finally {
                watcher.uninstall();
            }

            assertThat(firstBlocker.isBlocking()).isFalse();
            assertThat(secondBlocker.isBlocking()).isFalse();
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class))
                    .isNull();
        }

        @Test
        @DisplayName("Easy AI drops Okk when its greater-power partner is unaffordable")
        void dropsOkkWhenGreaterPowerPartnerIsUnaffordable() {
            Permanent attacker = combatHarness.addToBattlefieldAndReturn(opponent, new HillGiant());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            Permanent okk = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new Okk());
            okk.setSummoningSick(false);
            TestCards.mutableCard(okk).setPower(2);
            Permanent partner = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new Hipparion());
            partner.setSummoningSick(false);
            TestCards.mutableCard(partner).setPower(4);

            combatHarness.forceActivePlayer(opponent);
            combatHarness.forceStep(TurnStep.DECLARE_BLOCKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.beginBlockerDeclarationInput();

            int attackerIndex = combatGd.playerBattlefields.get(opponent.getId()).indexOf(attacker);
            int okkIndex = combatGd.playerBattlefields.get(combatAiPlayer.getId()).indexOf(okk);
            int partnerIndex = combatGd.playerBattlefields.get(combatAiPlayer.getId()).indexOf(partner);
            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                combatAi.sendBlockerDeclaration(new DeclareBlockersRequest(List.of(
                        new BlockerAssignment(okkIndex, attackerIndex),
                        new BlockerAssignment(partnerIndex, attackerIndex))));
                assertThat(watcher.drainFailures()).isEmpty();
            } finally {
                watcher.uninstall();
            }

            assertThat(okk.isBlocking()).isFalse();
            assertThat(partner.isBlocking()).isFalse();
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class))
                    .isNull();
        }

        @Test
        @DisplayName("Easy AI leaves Tromokratis unblocked when a defending creature cannot block")
        void leavesTromokratisUnblockedWhenADefendingCreatureCannotBlock() {
            Permanent attacker = combatHarness.addToBattlefieldAndReturn(opponent, new Tromokratis());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            Permanent firstBlocker = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            firstBlocker.setSummoningSick(false);
            Permanent secondBlocker = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new HillGiant());
            secondBlocker.setSummoningSick(false);
            secondBlocker.tap();

            combatHarness.forceActivePlayer(opponent);
            combatHarness.forceStep(TurnStep.DECLARE_BLOCKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.beginBlockerDeclarationInput();

            int firstBlockerIndex = combatGd.playerBattlefields.get(combatAiPlayer.getId()).indexOf(firstBlocker);
            int attackerIndex = combatGd.playerBattlefields.get(opponent.getId()).indexOf(attacker);
            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                combatAi.sendBlockerDeclaration(new DeclareBlockersRequest(List.of(
                        new BlockerAssignment(firstBlockerIndex, attackerIndex))));
                assertThat(watcher.drainFailures()).isEmpty();
            } finally {
                watcher.uninstall();
            }

            assertThat(firstBlocker.isBlocking()).isFalse();
            assertThat(secondBlocker.isBlocking()).isFalse();
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class))
                    .isNull();
        }

        @Test
        @DisplayName("Easy AI does not submit an unaffordable block tax from an attacking Archangel")
        void dropsUnaffordableGlobalBlockTax() {
            Permanent archangel = combatHarness.addToBattlefieldAndReturn(opponent, new ArchangelOfTithes());
            archangel.setSummoningSick(false);
            archangel.setAttacking(true);
            Permanent attacker = combatHarness.addToBattlefieldAndReturn(opponent, new GrizzlyBears());
            attacker.setSummoningSick(false);
            attacker.setAttacking(true);
            Permanent blocker = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new HillGiant());
            blocker.setSummoningSick(false);

            combatHarness.forceActivePlayer(opponent);
            combatHarness.forceStep(TurnStep.DECLARE_BLOCKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.beginBlockerDeclarationInput();

            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                combatAi.sendBlockerDeclaration(new DeclareBlockersRequest(List.of(new BlockerAssignment(
                        combatGd.playerBattlefields.get(combatAiPlayer.getId()).indexOf(blocker),
                        combatGd.playerBattlefields.get(opponent.getId()).indexOf(attacker)))));

                assertThat(watcher.drainFailures()).isEmpty();
            } finally {
                watcher.uninstall();
            }

            assertThat(blocker.isBlocking()).isFalse();
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class))
                    .isNull();
        }

        @Test
        @DisplayName("Easy AI removes a creature that can only attack alone from a larger group")
        void removesCanOnlyAttackAloneCreature() {
            Permanent restricted = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            restricted.setSummoningSick(false);
            Permanent aura = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new Errantry());
            aura.setAttachedTo(restricted.getId());
            Permanent unrestricted = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            unrestricted.setSummoningSick(false);

            List<Integer> result = combatAi.prepareAttackersForTax(combatGd, List.of(0, 2));

            assertThat(result).containsExactly(2);
        }

        @Test
        @DisplayName("Easy AI keeps one creature when every selected creature can only attack alone")
        void keepsOneWhenAllCanOnlyAttackAlone() {
            Permanent firstRestricted = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            firstRestricted.setSummoningSick(false);
            Permanent firstAura = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new Errantry());
            firstAura.setAttachedTo(firstRestricted.getId());
            Permanent secondRestricted = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            secondRestricted.setSummoningSick(false);
            Permanent secondAura = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new Errantry());
            secondAura.setAttachedTo(secondRestricted.getId());

            List<Integer> result = combatAi.prepareAttackersForTax(combatGd, List.of(0, 2));

            assertThat(result).containsExactly(0);
        }

        @Test
        @DisplayName("Easy AI includes an attack-if-another-attacks creature")
        void includesConditionalAttackRequirement() {
            combatGd.playerLifeTotals.put(opponent.getId(), 5);
            Permanent cyclops = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new EkunduCyclops());
            cyclops.setSummoningSick(false);
            Permanent bears = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            bears.setSummoningSick(false);

            combatHarness.forceActivePlayer(combatAiPlayer);
            combatHarness.forceStep(TurnStep.DECLARE_ATTACKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.inMutationScope(() ->
                    combatHarness.getCombatAttackService().handleDeclareAttackersStep(combatGd));

            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                combatAi.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

                assertThat(watcher.drainFailures()).isEmpty();
            } finally {
                watcher.uninstall();
            }

            assertThat(cyclops.isAttacking()).isTrue();
            assertThat(bears.isAttacking()).isTrue();
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class))
                    .isNull();
        }

        @Test
        @DisplayName("Easy AI chooses a legal attacker when forced and the first candidate is restricted")
        void doesNotDeclareOrcishConscriptsWithoutEnoughOtherAttackers() {
            Permanent conscripts = combatHarness.addToBattlefieldAndReturn(combatAiPlayer,
                    new OrcishConscripts());
            conscripts.setSummoningSick(false);
            Permanent ally = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            ally.setSummoningSick(false);
            Permanent trove = combatHarness.addToBattlefieldAndReturn(opponent, new TroveOfTemptation());
            trove.setSummoningSick(false);
            Permanent blocker = combatHarness.addToBattlefieldAndReturn(opponent, new AirElemental());
            blocker.setSummoningSick(false);

            combatHarness.forceActivePlayer(combatAiPlayer);
            combatHarness.forceStep(TurnStep.DECLARE_ATTACKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.beginAttackerDeclarationInput();

            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                combatAi.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

                assertThat(watcher.drainFailures()).isEmpty();
            } finally {
                watcher.uninstall();
            }

            assertThat(conscripts.isAttackedThisTurn()).isFalse();
            assertThat(ally.isAttackedThisTurn()).isTrue();
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class))
                    .isNull();
        }

        @Test
        @DisplayName("Easy AI ignores a stale attacker-declaration event")
        void ignoresStaleAttackerDeclarationEvent() {
            Permanent attacker = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            attacker.setSummoningSick(false);
            combatHarness.forceActivePlayer(combatAiPlayer);
            combatHarness.forceStep(TurnStep.DECLARE_ATTACKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.beginAttackerDeclarationInput();

            combatAi.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);
            TurnStep stepAfterDeclaration = combatGd.currentStep;
            int lifeAfterDeclaration = combatGd.getLife(opponent.getId());

            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                combatAi.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

                assertThat(watcher.drainFailures()).isEmpty();
            } finally {
                watcher.uninstall();
            }
            assertThat(lifeAfterDeclaration).isEqualTo(18);
            assertThat(combatGd.currentStep).isEqualTo(stepAfterDeclaration);
            assertThat(combatGd.getLife(opponent.getId())).isEqualTo(lifeAfterDeclaration);
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
        }

        @Test
        @DisplayName("Easy AI does not submit a creature barred by Island Sanctuary")
        void doesNotSubmitCreatureBarredByIslandSanctuary() {
            combatHarness.addToBattlefield(opponent, new IslandSanctuary());
            combatGd.turnNumber = 2;
            combatHarness.forceActivePlayer(opponent);
            combatHarness.forceStep(TurnStep.UPKEEP);
            combatHarness.clearPriorityPassed();
            combatHarness.passBothPriorities();
            combatHarness.handleMayAbilityChosen(opponent, true);

            Permanent attacker = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            attacker.setSummoningSick(false);
            combatHarness.forceActivePlayer(combatAiPlayer);
            combatHarness.forceStep(TurnStep.DECLARE_ATTACKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.beginAttackerDeclarationInput();

            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                combatAi.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

                assertThat(watcher.drainFailures()).isEmpty();
            } finally {
                watcher.uninstall();
            }
            assertThat(attacker.isAttacking()).isFalse();
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
        }

        @Test
        @DisplayName("Easy AI sends a required attacker to an available planeswalker")
        void sendsRequiredAttackerToAvailablePlaneswalker() {
            combatHarness.addToBattlefield(opponent, new IslandSanctuary());
            Permanent planeswalker = combatHarness.addToBattlefieldAndReturn(opponent, new JaceBeleren());
            planeswalker.setCounterCount(CounterType.LOYALTY, 3);
            Permanent blocker = combatHarness.addToBattlefieldAndReturn(opponent, new GrizzlyBears());
            blocker.setSummoningSick(false);
            combatGd.turnNumber = 2;
            combatHarness.forceActivePlayer(opponent);
            combatHarness.forceStep(TurnStep.UPKEEP);
            combatHarness.clearPriorityPassed();
            combatHarness.passBothPriorities();
            combatHarness.handleMayAbilityChosen(opponent, true);

            Permanent attacker = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new Ramroller());
            attacker.setSummoningSick(false);
            combatHarness.forceActivePlayer(combatAiPlayer);
            combatHarness.forceStep(TurnStep.DECLARE_ATTACKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.inMutationScope(() ->
                    combatHarness.getCombatAttackService().handleDeclareAttackersStep(combatGd));

            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                combatAi.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

                assertThat(watcher.drainFailures()).isEmpty();
            } finally {
                watcher.uninstall();
            }
            assertThat(attacker.isAttacking()).isTrue();
            assertThat(attacker.getAttackTarget()).isEqualTo(planeswalker.getId());
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class))
                    .isNull();
        }

        @Test
        @DisplayName("Easy AI respects a controller-scoped attacker limit")
        void respectsControllerScopedAttackerLimit() {
            combatGd.playerLifeTotals.put(opponent.getId(), 20);
            Permanent limit = combatHarness.addToBattlefieldAndReturn(opponent, new Crawlspace());
            limit.setSummoningSick(false);
            Permanent first = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            first.setSummoningSick(false);
            Permanent second = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            second.setSummoningSick(false);
            Permanent third = combatHarness.addToBattlefieldAndReturn(combatAiPlayer, new GrizzlyBears());
            third.setSummoningSick(false);

            combatHarness.forceActivePlayer(combatAiPlayer);
            combatHarness.forceStep(TurnStep.DECLARE_ATTACKERS);
            combatHarness.clearPriorityPassed();
            combatHarness.beginAttackerDeclarationInput();

            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                combatAi.sendAttackerDeclaration(new DeclareAttackersRequest(List.of(0, 1, 2), null));

                assertThat(watcher.drainFailures()).isEmpty();
            } finally {
                watcher.uninstall();
            }

            assertThat(combatGd.getLife(opponent.getId())).isEqualTo(16);
            assertThat(combatGd.gameLog.stream().map(entry -> entry.plainText()))
                    .anyMatch(log -> log.contains("declares 2 attackers."));
            assertThat(combatGd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class))
                    .isNull();
        }
    }

    // ===== Attack tax handling =====

    @Test
    @DisplayName("Easy AI caps attackers to affordable count when attack tax is present")
    void capsAttackersWhenAttackTaxPresent() throws Exception {
        gd.currentStep = TurnStep.DECLARE_ATTACKERS;
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(gd.activePlayerId));

        // 3 creatures on the AI battlefield
        for (int i = 0; i < 3; i++) {
            Permanent creature = new Permanent(new Card());
            TestCards.mutableCard(creature).setName("Bear " + i);
            TestCards.mutableCard(creature).setType(CardType.CREATURE);
            TestCards.mutableCard(creature).setPower(2);
            TestCards.mutableCard(creature).setToughness(2);
            creature.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(creature);
        }

        // AI has 2 mana in pool A?€�t tax is {1} per creature, so can afford at most 2
        gd.playerManaPools.get(aiPlayer.getId()).add(ManaColor.COLORLESS, 2);

        when(combatAttackService.getAttackableCreatureIndices(gd, aiPlayer.getId()))
                .thenReturn(List.of(0, 1, 2));
        when(combatAttackService.getMustAttackIndices(eq(gd), eq(aiPlayer.getId()), any()))
                .thenReturn(List.of());
        when(castingCostService.getAttackPaymentPerCreature(gd, aiPlayer.getId()))
                .thenReturn(1);
        when(gameQueryService.getEffectivePower(eq(gd), any())).thenReturn(2);
        when(gameQueryService.getEffectiveToughness(eq(gd), any())).thenReturn(2);

        createEngine().handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        ArgumentCaptor<DeclareAttackersRequest> captor = ArgumentCaptor.forClass(DeclareAttackersRequest.class);
        verify(messageHandler).handleDeclareAttackers(captor.capture());

        assertThat(captor.getValue().attackerIndices()).hasSizeLessThanOrEqualTo(2);
    }

    // ===== Rejected attacker declarations =====

    /**
     * Two 2/2s facing an empty board, so the Easy AI's own pick is the whole team and the
     * fallback ladder is what changes the declaration.
     */
    private void giveAiTwoUnopposedAttackers() {
        gd.currentStep = TurnStep.DECLARE_ATTACKERS;
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(gd.activePlayerId));

        for (int i = 0; i < 2; i++) {
            Permanent creature = new Permanent(new Card());
            TestCards.mutableCard(creature).setName("Bear " + i);
            TestCards.mutableCard(creature).setType(CardType.CREATURE);
            TestCards.mutableCard(creature).setPower(2);
            TestCards.mutableCard(creature).setToughness(2);
            creature.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(creature);
        }

        when(combatAttackService.getAttackableCreatureIndices(gd, aiPlayer.getId()))
                .thenReturn(List.of(0, 1));
        when(combatAttackService.getAttackableCreatureIndicesForTarget(
                eq(gd), eq(aiPlayer.getId()), any())).thenReturn(List.of(0, 1));
        when(combatAttackService.getMustAttackIndices(eq(gd), eq(aiPlayer.getId()), any()))
                .thenReturn(List.of());
        when(gameQueryService.getEffectivePower(eq(gd), any())).thenReturn(2);
        when(gameQueryService.getEffectiveToughness(eq(gd), any())).thenReturn(2);
    }

    @Test
    @DisplayName("A rejected attacker declaration falls back to attacking with nothing")
    void fallsBackToNoAttackersWhenDeclarationRejected() throws Exception {
        giveAiTwoUnopposedAttackers();

        AtomicInteger attempts = new AtomicInteger();
        when(messageHandler.handleDeclareAttackers(any()))
                .thenAnswer(invocation -> attempts.incrementAndGet() == 1 ? "Okk can't attack" : null);

        createEngine().handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        ArgumentCaptor<DeclareAttackersRequest> captor = ArgumentCaptor.forClass(DeclareAttackersRequest.class);
        verify(messageHandler, times(2)).handleDeclareAttackers(captor.capture());
        assertThat(captor.getAllValues().get(0).attackerIndices()).isNotEmpty();
        assertThat(captor.getAllValues().get(1).attackerIndices()).isEmpty();
    }

    @Test
    @DisplayName("When attacking with nothing is rejected too, the AI attacks with everything able")
    void fallsBackToEveryAttackerWhenNoAttackersAlsoRejected() throws Exception {
        giveAiTwoUnopposedAttackers();

        AtomicInteger attempts = new AtomicInteger();
        when(messageHandler.handleDeclareAttackers(any()))
                .thenAnswer(invocation -> attempts.incrementAndGet() < 3
                        ? "Creature at index 1 must attack this combat"
                        : null);

        createEngine().handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        ArgumentCaptor<DeclareAttackersRequest> captor = ArgumentCaptor.forClass(DeclareAttackersRequest.class);
        verify(messageHandler, times(3)).handleDeclareAttackers(captor.capture());
        assertThat(captor.getAllValues().get(1).attackerIndices()).isEmpty();
        assertThat(captor.getAllValues().get(2).attackerIndices()).containsExactly(0, 1);
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

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(captor.capture());

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

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
        verify(messageHandler).handlePlayCard(captor.capture());

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

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler).handlePassPriority(any());
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

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler).handlePassPriority(any());
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
        }).when(messageHandler).handlePlayCard(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        verify(messageHandler).handlePlayCard(any());
    }

    // ===== Entrancing Melody (PermanentManaValueEqualsXPredicate) A?€�t harness-based =====

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
                    testHarness.getGameService(), testHarness.getGameQueryService(),
                    testHarness.getBlockLegalityService(), testHarness.getCombatAttackService(), testHarness.getGameActionAvailabilityService(), testHarness.getCastingCostService(), testHarness.getCastingPermissionService(),
                    testHarness.getTargetValidationService(), testHarness.getTargetLegalityService());
        }

        private void giveAiPriorityLocal() {
            testHarness.forceActivePlayer(aiTestPlayer);
            testHarness.forceStep(TurnStep.PRECOMBAT_MAIN);
            testHarness.clearPriorityPassed();
            testGd.status = GameStatus.RUNNING;
            testGd.interaction.clearAwaitingInput();
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
        @DisplayName("Easy AI uses Pyrokinesis's hand-exile alternate cost")
        void usesHandExileAlternateCost() {
            giveAiPriorityLocal();
            Permanent target = testHarness.addToBattlefieldAndReturn(human, new GrizzlyBears());
            target.setSummoningSick(false);
            Pyrokinesis pyrokinesis = new Pyrokinesis();
            LightningBolt redCard = new LightningBolt();
            testHarness.setHand(aiTestPlayer, List.of(pyrokinesis, redCard));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(pyrokinesis);
            assertThat(testGd.getPlayerExiledCards(aiTestPlayer.getId())).containsExactly(redCard);
            assertThat(testGd.playerManaPools.get(aiTestPlayer.getId()).get(ManaColor.RED)).isZero();
        }

        @Test
        @DisplayName("Easy AI exiles only cards matching an ExileX graveyard cost")
        void exilesOnlyMatchingCardsForExileXCost() {
            giveAiPriorityLocal();

            Card spell = new Card();
            spell.setName("Mixed Graveyard Spell");
            spell.setType(CardType.SORCERY);
            spell.setManaCost("{B}");
            spell.addEffect(EffectSlot.SPELL,
                    new com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost(CardType.CREATURE));

            testHarness.setGraveyard(aiTestPlayer, List.of(
                    new HolyDay(), new GrizzlyBears(), new HolyDay()));
            testHarness.setHand(aiTestPlayer, List.of(spell));
            testHarness.addMana(aiTestPlayer, ManaColor.BLACK, 1);

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.getPlayerExiledCards(aiTestPlayer.getId()))
                    .extracting(Card::getName)
                    .containsExactly("Grizzly Bears");
            assertThat(testGd.playerGraveyards.get(aiTestPlayer.getId()))
                    .extracting(Card::getName)
                    .containsExactly("Holy Day", "Holy Day");
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

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard().getName()).isEqualTo("Entrancing Melody");
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
            assertThat(testGd.stack.getFirst().getXValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("Easy AI casts Dominate with a target within the announced X")
        void castsDominateWithTargetWithinAnnouncedX() {
            giveAiPriorityLocal();
            giveAiIslandsLocal(6); // maxX = 3; the target determines X=2

            Permanent tooExpensive = new Permanent(new HillGiant()); // MV=4
            tooExpensive.setSummoningSick(false);
            testGd.playerBattlefields.get(human.getId()).add(tooExpensive);

            Permanent target = new Permanent(new GrizzlyBears()); // MV=2
            target.setSummoningSick(false);
            testGd.playerBattlefields.get(human.getId()).add(target);

            Dominate dominate = new Dominate();
            testHarness.setHand(aiTestPlayer, List.of(dominate));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(dominate);
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
            assertThat(testGd.stack.getFirst().getTargetId()).isNotEqualTo(tooExpensive.getId());
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

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

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

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).isEmpty();
        }

        @Test
        @DisplayName("Easy AI caps Open the Way at the number of players")
        void capsOpenTheWayAtTheNumberOfPlayers() {
            giveAiPriorityLocal();
            testHarness.addMana(aiTestPlayer, ManaColor.GREEN, 4);
            OpenTheWay openTheWay = new OpenTheWay();
            testHarness.setHand(aiTestPlayer, List.of(openTheWay));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard()).isSameAs(openTheWay);
            assertThat(testGd.stack.getFirst().getXValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("Easy AI respects a binding X value cap instead of attempting an illegal cast")
        void respectsBindingXValueCap() {
            FuzzLogWatcher watcher = FuzzLogWatcher.install();
            try {
                giveAiPriorityLocal();
                giveAiIslandsLocal(4); // maxX = 2 on mana alone

                Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
                bears.setSummoningSick(false);
                testGd.playerBattlefields.get(human.getId()).add(bears);

                // Hypothetical "X can't be greater than the number of snow lands you control"
                // (the shape Winter's Chill uses). The AI controls plain Islands, so the cap is 0.
                EntrancingMelody capped = new EntrancingMelody();
                capped.setXValueCap(new PermanentCount(
                        new PermanentHasSupertypePredicate(CardSupertype.SNOW), CountScope.CONTROLLER));
                testHarness.setHand(aiTestPlayer, List.of(capped));

                easyAi.handleEvent(AiDecisionKind.GAME_STATE);

                // The spell is uncastable either way, so an empty stack proves nothing on its own.
                // What the clamp changes is whether the AI ANNOUNCES an illegal X=2 and has
                // SpellCastingService reject it ("X can't be greater than 0"), burning its
                // priority. That rejection surfaces as a "PlayCard failed silently" disagreement.
                assertThat(watcher.drainFailures()).isEmpty();
                assertThat(testGd.stack).isEmpty();
            } finally {
                watcher.uninstall();
            }
        }

        @Test
        @DisplayName("Easy AI still casts when the X value cap is not binding")
        void castsWhenXValueCapNotBinding() {
            giveAiPriorityLocal();
            giveAiIslandsLocal(4); // maxX = 2

            Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
            bears.setSummoningSick(false);
            testGd.playerBattlefields.get(human.getId()).add(bears);

            // Cap counts lands you control (4) — above the affordable X, so it must not restrict.
            EntrancingMelody capped = new EntrancingMelody();
            capped.setXValueCap(
                    new PermanentCount(new PermanentIsLandPredicate(), CountScope.CONTROLLER));
            testHarness.setHand(aiTestPlayer, List.of(capped));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
            assertThat(testGd.stack.getFirst().getXValue()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Easy AI attacks with at least one creature when forced by opponent effect")
    void attacksWithAtLeastOneWhenForcedByOpponentEffect() throws Exception {
        gd.currentStep = TurnStep.DECLARE_ATTACKERS;
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(gd.activePlayerId));

        // AI has a 2/2
        Permanent creature = new Permanent(new Card());
        TestCards.mutableCard(creature).setName("Bear");
        TestCards.mutableCard(creature).setType(CardType.CREATURE);
        TestCards.mutableCard(creature).setPower(2);
        TestCards.mutableCard(creature).setToughness(2);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(creature);

        // Opponent has a 5/5 blocker A?€�t AI would normally choose not to attack
        UUID opponentId = gd.orderedPlayerIds.get(1);
        Permanent blocker = new Permanent(new Card());
        TestCards.mutableCard(blocker).setName("Big Blocker");
        TestCards.mutableCard(blocker).setType(CardType.CREATURE);
        TestCards.mutableCard(blocker).setPower(5);
        TestCards.mutableCard(blocker).setToughness(5);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(opponentId).add(blocker);

        when(combatAttackService.getAttackableCreatureIndices(gd, aiPlayer.getId()))
                .thenReturn(List.of(0));
        when(combatAttackService.getAttackableCreatureIndicesForTarget(gd, aiPlayer.getId(), opponentId))
                .thenReturn(List.of(0));
        when(combatAttackService.getMustAttackIndices(eq(gd), eq(aiPlayer.getId()), any()))
                .thenReturn(List.of());
        when(combatAttackService.isOpponentForcedToAttack(gd, aiPlayer.getId()))
                .thenReturn(true);
        when(castingCostService.getAttackPaymentPerCreature(gd, aiPlayer.getId()))
                .thenReturn(0);
        when(gameQueryService.getEffectivePower(eq(gd), any())).thenReturn(2);
        when(gameQueryService.getEffectiveToughness(eq(gd), any())).thenReturn(2);
        when(blockLegalityService.canBlock(nullable(BlockLegalityContext.class), eq(blocker))).thenReturn(true);
        when(blockLegalityService.canBlockAttacker(any(), eq(blocker), any())).thenReturn(true);
        when(gameQueryService.getEffectivePower(gd, blocker)).thenReturn(5);
        when(gameQueryService.getEffectiveToughness(gd, blocker)).thenReturn(5);

        createEngine().handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        ArgumentCaptor<DeclareAttackersRequest> captor = ArgumentCaptor.forClass(DeclareAttackersRequest.class);
        verify(messageHandler).handleDeclareAttackers(captor.capture());

        // Must declare at least one attacker despite unfavorable board
        assertThat(captor.getValue().attackerIndices()).isNotEmpty();
    }

    @Test
    @DisplayName("Easy AI can declare zero attackers when not forced by opponent effect")
    void canDeclareZeroAttackersWhenNotForced() throws Exception {
        gd.currentStep = TurnStep.DECLARE_ATTACKERS;
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(gd.activePlayerId));

        // AI has a 2/2 but strong opponent blocker A?€�t Easy AI should choose not to attack
        Permanent creature = new Permanent(new Card());
        TestCards.mutableCard(creature).setName("Bear");
        TestCards.mutableCard(creature).setType(CardType.CREATURE);
        TestCards.mutableCard(creature).setPower(2);
        TestCards.mutableCard(creature).setToughness(2);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(creature);

        UUID opponentId = gd.orderedPlayerIds.get(1);
        Permanent blocker = new Permanent(new Card());
        TestCards.mutableCard(blocker).setName("Big Blocker");
        TestCards.mutableCard(blocker).setType(CardType.CREATURE);
        TestCards.mutableCard(blocker).setPower(5);
        TestCards.mutableCard(blocker).setToughness(5);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(opponentId).add(blocker);

        when(combatAttackService.getAttackableCreatureIndices(gd, aiPlayer.getId()))
                .thenReturn(List.of(0));
        when(combatAttackService.getMustAttackIndices(eq(gd), eq(aiPlayer.getId()), any()))
                .thenReturn(List.of());
        when(combatAttackService.isOpponentForcedToAttack(gd, aiPlayer.getId()))
                .thenReturn(false);
        when(castingCostService.getAttackPaymentPerCreature(gd, aiPlayer.getId()))
                .thenReturn(0);
        when(gameQueryService.getEffectivePower(eq(gd), any())).thenReturn(2);
        when(gameQueryService.getEffectiveToughness(eq(gd), any())).thenReturn(2);
        when(blockLegalityService.canBlock(nullable(BlockLegalityContext.class), eq(blocker))).thenReturn(true);
        when(blockLegalityService.canBlockAttacker(any(), eq(blocker), any())).thenReturn(true);
        when(gameQueryService.getEffectivePower(gd, blocker)).thenReturn(5);
        when(gameQueryService.getEffectiveToughness(gd, blocker)).thenReturn(5);

        createEngine().handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        ArgumentCaptor<DeclareAttackersRequest> captor = ArgumentCaptor.forClass(DeclareAttackersRequest.class);
        verify(messageHandler).handleDeclareAttackers(captor.capture());

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
            gd.interaction.beginInteraction(new PendingInteraction.ColorChoice(null, null, null, null, java.util.List.of(), "Choose a color."));
            return null;
        }).when(messageHandler).handleTapPermanent(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        // AI should have tapped the land but NOT cast the spell or passed priority
        verify(messageHandler).handleTapPermanent(any());
        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler, never()).handlePassPriority(any());
    }

    @Test
    @DisplayName("Easy AI does not cast a spell when mana tapping puts a trigger on the stack")
    void doesNotCastSpellWhenManaTappingPutsTriggerOnStack() throws Exception {
        Card creature = new Card();
        creature.setName("Test Knight");
        creature.setType(CardType.CREATURE);
        creature.setManaCost("{W}");
        creature.setPower(2);
        creature.setToughness(2);
        gd.playerHands.get(aiPlayer.getId()).add(creature);

        Permanent land = new Permanent(new Plains());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(land);

        when(gameQueryService.canActivateManaAbility(any(), any())).thenReturn(true);
        Mockito.doAnswer(inv -> {
            gd.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature,
                    aiPlayer.getId(), "Test trigger", List.of()));
            return null;
        }).when(messageHandler).handleTapPermanent(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        verify(messageHandler).handleTapPermanent(any());
        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler, never()).handlePassPriority(any());
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
            gd.interaction.beginInteraction(new PendingInteraction.ColorChoice(null, null, null, null, java.util.List.of(), "Choose a color."));
            return null;
        }).when(messageHandler).handleTapPermanent(any());

        createEngine().handleEvent(AiDecisionKind.GAME_STATE);

        verify(messageHandler).handleTapPermanent(any());
        verify(messageHandler, never()).handlePlayCard(any());
        verify(messageHandler, never()).handlePassPriority(any());
    }

    // ===== Targeting tax handling A?€�t harness-based =====

    @Nested
    @DisplayName("Targeting tax (Kopala, Warden of Waves)")
    class TargetingTaxTests {

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
                    testHarness.getGameService(), testHarness.getGameQueryService(),
                    testHarness.getBlockLegalityService(), testHarness.getCombatAttackService(), testHarness.getGameActionAvailabilityService(), testHarness.getCastingCostService(), testHarness.getCastingPermissionService(),
                    testHarness.getTargetValidationService(), testHarness.getTargetLegalityService());
        }

        private void giveAiPriorityLocal() {
            testHarness.forceActivePlayer(aiTestPlayer);
            testHarness.forceStep(TurnStep.PRECOMBAT_MAIN);
            testHarness.clearPriorityPassed();
            testGd.status = GameStatus.RUNNING;
            testGd.interaction.clearAwaitingInput();
            testGd.stack.clear();
        }

        private void giveAiPlainsLocal(int count) {
            for (int i = 0; i < count; i++) {
                Permanent plains = new Permanent(new Plains());
                plains.setSummoningSick(false);
                testGd.playerBattlefields.get(aiTestPlayer.getId()).add(plains);
            }
        }

        private void giveAiMountainsLocal(int count) {
            for (int i = 0; i < count; i++) {
                Permanent mountain = new Permanent(new com.github.laxika.magicalvibes.cards.m.Mountain());
                mountain.setSummoningSick(false);
                testGd.playerBattlefields.get(aiTestPlayer.getId()).add(mountain);
            }
        }

        @Test
        @DisplayName("Easy AI does not cast Pacifism when targeting tax makes it unaffordable")
        void doesNotCastPacifismWhenTargetingTaxMakesUnaffordable() {
            giveAiPriorityLocal();
            giveAiPlainsLocal(2); // Only 2 mana A?€�t Pacifism costs {1}{W} but Kopala adds {2}

            Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
            kopala.setSummoningSick(false);
            testGd.playerBattlefields.get(human.getId()).add(kopala);

            testHarness.setHand(aiTestPlayer, List.of(new com.github.laxika.magicalvibes.cards.p.Pacifism()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            // Should NOT cast A?€�t can't afford {1}{W} + {2} tax = 4 mana with only 2 Plains
            assertThat(testGd.stack).isEmpty();
            assertThat(testGd.playerBattlefields.get(aiTestPlayer.getId()))
                    .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                    .allMatch(permanent -> !permanent.isTapped());
        }

        @Test
        @DisplayName("Easy AI casts Pacifism when it can afford targeting tax")
        void castsPacifismWhenCanAffordTargetingTax() {
            giveAiPriorityLocal();
            giveAiPlainsLocal(4); // 4 mana A?€�t enough for {1}{W} + {2} tax

            Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
            kopala.setSummoningSick(false);
            testGd.playerBattlefields.get(human.getId()).add(kopala);

            testHarness.setHand(aiTestPlayer, List.of(new com.github.laxika.magicalvibes.cards.p.Pacifism()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(testGd.stack).hasSize(1);
            assertThat(testGd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
            assertThat(testGd.playerBattlefields.get(aiTestPlayer.getId()))
                    .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                    .allMatch(Permanent::isTapped);
        }

        @Test
        @DisplayName("Easy AI does not cast instant when targeting tax makes it unaffordable")
        void doesNotCastInstantWhenTargetingTaxMakesUnaffordable() {
            // Set up on opponent's turn outside main phase so tryCastInstant is triggered
            testHarness.forceActivePlayer(human);
            testHarness.forceStep(TurnStep.POSTCOMBAT_MAIN);
            testHarness.clearPriorityPassed();
            testGd.status = GameStatus.RUNNING;
            testGd.interaction.clearAwaitingInput();
            testGd.stack.clear();

            giveAiMountainsLocal(1); // Only 1 mana A?€�t Bolt costs {R} but Kopala adds {2}

            Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
            kopala.setSummoningSick(false);
            testGd.playerBattlefields.get(human.getId()).add(kopala);

            testHarness.setHand(aiTestPlayer, List.of(new com.github.laxika.magicalvibes.cards.l.LightningBolt()));

            easyAi.handleEvent(AiDecisionKind.GAME_STATE);

            // Should NOT cast A?€�t can't afford {R} + {2} tax = 3 mana with only 1 Mountain
            assertThat(testGd.stack).isEmpty();
        }
    }

    // ===== May-pay mana floating (base AiDecisionEngine behavior) =====

    @Nested
    @DisplayName("May-pay mana floating")
    class MayPayFloatingTests {

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
                    testHarness.getGameService(), testHarness.getGameQueryService(),
                    testHarness.getBlockLegalityService(), testHarness.getCombatAttackService(), testHarness.getGameActionAvailabilityService(), testHarness.getCastingCostService(), testHarness.getCastingPermissionService(),
                    testHarness.getTargetValidationService(), testHarness.getTargetLegalityService());
        }

        /** Human casts a red spell so the AI's Iron Star trigger resolves into the may-pay prompt. */
        private void fireIronStarTrigger() {
            testHarness.forceActivePlayer(human);
            testHarness.forceStep(TurnStep.PRECOMBAT_MAIN);
            testHarness.clearPriorityPassed();
            testGd.status = GameStatus.RUNNING;
            testHarness.setHand(human, List.of(new com.github.laxika.magicalvibes.cards.b.BogardanFirefiend()));
            testHarness.addMana(human, ManaColor.RED, 3);
            testHarness.castCreature(human, 0);
            testHarness.passBothPriorities();
            assertThat(testGd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                    .isEqualTo(aiTestPlayer.getId());
        }

        @Test
        @DisplayName("Easy AI taps a land to pay a may-cost instead of accepting into a fizzle")
        void tapsLandToPayMayCost() {
            testHarness.addToBattlefield(aiTestPlayer, new com.github.laxika.magicalvibes.cards.i.IronStar());
            Permanent mountain = new Permanent(new com.github.laxika.magicalvibes.cards.m.Mountain());
            mountain.setSummoningSick(false);
            testGd.playerBattlefields.get(aiTestPlayer.getId()).add(mountain);

            int lifeBefore = testGd.playerLifeTotals.get(aiTestPlayer.getId());
            fireIronStarTrigger();

            easyAi.handleEvent(AiDecisionKind.INTERACTION);

            assertThat(testGd.playerLifeTotals.get(aiTestPlayer.getId())).isEqualTo(lifeBefore + 1);
            assertThat(mountain.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Easy AI declines a may-cost it cannot pay")
        void declinesUnpayableMayCost() {
            testHarness.addToBattlefield(aiTestPlayer, new com.github.laxika.magicalvibes.cards.i.IronStar());

            int lifeBefore = testGd.playerLifeTotals.get(aiTestPlayer.getId());
            fireIronStarTrigger();

            easyAi.handleEvent(AiDecisionKind.INTERACTION);

            assertThat(testGd.playerLifeTotals.get(aiTestPlayer.getId())).isEqualTo(lifeBefore);
            assertThat(testGd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        }

        @Test
        @DisplayName("Easy AI chooses a legal X when a pay-X trigger has a fixed mana component")
        void choosesLegalXForFlameblastDragon() {
            Permanent dragon = testHarness.addToBattlefieldAndReturn(aiTestPlayer, new FlameblastDragon());
            dragon.setSummoningSick(false);
            for (int i = 0; i < 4; i++) {
                Permanent mountain = testHarness.addToBattlefieldAndReturn(aiTestPlayer, new Mountain());
                mountain.setSummoningSick(false);
            }
            Permanent target = testHarness.addToBattlefieldAndReturn(human, new AirElemental());
            target.setSummoningSick(false);
            testHarness.setHand(aiTestPlayer, List.of());

            testHarness.forceActivePlayer(aiTestPlayer);
            testHarness.forceStep(TurnStep.DECLARE_ATTACKERS);
            testHarness.clearPriorityPassed();
            testGd.status = GameStatus.RUNNING;
            testHarness.beginAttackerDeclarationInput();

            int dragonIndex = testGd.playerBattlefields.get(aiTestPlayer.getId()).indexOf(dragon);
            testHarness.getGameService().declareAttackers(testGd, aiTestPlayer, List.of(dragonIndex));
            testHarness.handlePermanentChosen(aiTestPlayer, target.getId());
            testHarness.passBothPriorities();

            assertThat(testGd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();

            easyAi.handleEvent(AiDecisionKind.INTERACTION);

            assertThat(target.getMarkedDamage()).isEqualTo(3);
            assertThat(testGd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNull();
            assertThat(testGd.playerManaPools.get(aiTestPlayer.getId()).getTotal()).isZero();
        }
    }
}
