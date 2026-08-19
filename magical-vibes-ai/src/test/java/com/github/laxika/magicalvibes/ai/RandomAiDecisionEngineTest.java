package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AwesomePresence;
import com.github.laxika.magicalvibes.cards.a.AlphaAuthority;
import com.github.laxika.magicalvibes.cards.a.AbandonHope;
import com.github.laxika.magicalvibes.cards.a.AladdinsRing;
import com.github.laxika.magicalvibes.cards.b.BackFromTheBrink;
import com.github.laxika.magicalvibes.cards.b.BalmOfRestoration;
import com.github.laxika.magicalvibes.cards.c.Confiscate;
import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.c.CulturalExchange;
import com.github.laxika.magicalvibes.cards.d.DigThroughTime;
import com.github.laxika.magicalvibes.cards.d.DerangedAssistant;
import com.github.laxika.magicalvibes.cards.d.DeathsDuet;
import com.github.laxika.magicalvibes.cards.d.DuelingGrounds;
import com.github.laxika.magicalvibes.cards.d.DauthiMercenary;
import com.github.laxika.magicalvibes.cards.d.Dominate;
import com.github.laxika.magicalvibes.cards.d.Drought;
import com.github.laxika.magicalvibes.cards.e.Errantry;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.h.HeartlessSummoning;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Hipparion;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.j.JacesSanctum;
import com.github.laxika.magicalvibes.cards.k.KjeldoranRoyalGuard;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LuminousRebuke;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LilianasIndignation;
import com.github.laxika.magicalvibes.cards.l.Lure;
import com.github.laxika.magicalvibes.cards.m.MagneticWeb;
import com.github.laxika.magicalvibes.cards.m.Mathemagics;
import com.github.laxika.magicalvibes.cards.m.Mindslaver;
import com.github.laxika.magicalvibes.cards.m.MogissMarauder;
import com.github.laxika.magicalvibes.cards.o.Okk;
import com.github.laxika.magicalvibes.cards.o.OrcishConscripts;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PullFromTheDeep;
import com.github.laxika.magicalvibes.cards.p.PhyrexianTribute;
import com.github.laxika.magicalvibes.cards.p.PhyrexianPurge;
import com.github.laxika.magicalvibes.cards.p.PedanticLearning;
import com.github.laxika.magicalvibes.cards.p.PrimitiveJustice;
import com.github.laxika.magicalvibes.cards.p.Pyrokinesis;
import com.github.laxika.magicalvibes.cards.p.PyrrhicStrike;
import com.github.laxika.magicalvibes.cards.r.ReturnToTheRanks;
import com.github.laxika.magicalvibes.cards.r.Ramroller;
import com.github.laxika.magicalvibes.cards.r.RiskFactor;
import com.github.laxika.magicalvibes.cards.s.SchemingSymmetry;
import com.github.laxika.magicalvibes.cards.s.SetessanTactics;
import com.github.laxika.magicalvibes.cards.s.SoldeviAdnate;
import com.github.laxika.magicalvibes.cards.s.StormCauldron;
import com.github.laxika.magicalvibes.cards.s.StirTheGrave;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.StrengthOfTheTajuru;
import com.github.laxika.magicalvibes.cards.t.TorrentOfSouls;
import com.github.laxika.magicalvibes.cards.t.TolarianScholar;
import com.github.laxika.magicalvibes.cards.t.TorgaarFamineIncarnate;
import com.github.laxika.magicalvibes.cards.v.Victimize;
import com.github.laxika.magicalvibes.cards.w.WintersChill;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class RandomAiDecisionEngineTest {

    @Test
    void suppliesTargetForNestedMillFollowUpSpell() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.setLibrary(aiPlayer, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(aiPlayer, ManaColor.BLACK, 4);
        harness.setHand(aiPlayer, List.of(new LilianasIndignation()));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getTargetId())
                    .isIn(aiPlayer.getId(), opponent.getId());
            UUID targetId = gameData.stack.getFirst().getTargetId();
            int targetLifeBefore = gameData.getLife(targetId);

            harness.passBothPriorities();

            assertThat(watcher.drainFailures()).isEmpty();
            assertThat(gameData.getLife(targetId)).isEqualTo(targetLifeBefore - 6);
        } finally {
            watcher.uninstall();
        }
    }

    @Test
    void selectsModeBeforeActivatingModalAbility() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(aiPlayer, new BalmOfRestoration());
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 1);
        harness.setHand(aiPlayer, List.of());
        int lifeBefore = gameData.getLife(aiPlayer.getId());

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getXValue()).isZero();

        harness.passBothPriorities();

        assertThat(gameData.getLife(aiPlayer.getId())).isEqualTo(lifeBefore + 2);
        harness.assertInGraveyard(aiPlayer, "Balm of Restoration");
    }

    @Test
    void doesNotRetryCastAfterLibraryMovingManaAbilityOpensStack() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(aiPlayer, new PedanticLearning());
        Permanent assistant = harness.addToBattlefieldAndReturn(aiPlayer, new DerangedAssistant());
        assistant.setSummoningSick(false);
        for (int i = 0; i < 7; i++) {
            Permanent island = harness.addToBattlefieldAndReturn(aiPlayer, new Island());
            island.setSummoningSick(false);
        }
        harness.setLibrary(aiPlayer, List.of(new Forest()));
        AladdinsRing ring = new AladdinsRing();
        harness.setHand(aiPlayer, List.of(ring));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.playerHands.get(aiPlayer.getId())).containsExactly(ring);
        assertThat(gameData.stack).isNotEmpty();
        assertThat(gameData.stack).noneMatch(entry -> entry.getCard() == ring);
        assertThat(assistant.isTapped()).isTrue();
    }

    @Test
    void castsRepeatedXSpellWithGenericCostReduction() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(aiPlayer, new JacesSanctum());
        harness.addMana(aiPlayer, ManaColor.BLUE, 8);
        Mathemagics mathemagics = new Mathemagics();
        harness.setHand(aiPlayer, List.of(mathemagics));

        RandomAiDecisionEngine engine = createEngine(harness, aiPlayer, new Random() {
            @Override
            public boolean nextBoolean() {
                return true;
            }

            @Override
            public int nextInt(int bound) {
                return bound - 1;
            }
        });
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        StackEntry spell = gameData.stack.stream()
                .filter(entry -> entry.getCard() == mathemagics)
                .findFirst()
                .orElseThrow();
        assertThat(spell.getXValue()).isEqualTo(3);
    }

    @Test
    void castsPrimitiveJusticeWithItsBaseTargetCount() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent artifact = harness.addToBattlefieldAndReturn(opponent, new Ornithopter());
        harness.addMana(aiPlayer, ManaColor.RED, 2);
        PrimitiveJustice primitiveJustice = new PrimitiveJustice();
        harness.setHand(aiPlayer, List.of(primitiveJustice));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(primitiveJustice);
        assertThat(gameData.stack.getFirst().getXValue()).isEqualTo(1);
        assertThat(gameData.stack.getFirst().getTargetId()).isEqualTo(artifact.getId());
        assertThat(gameData.stack.getFirst().getRepeatedAdditionalCosts()).isEmpty();
    }

    @Test
    void castsRiskFactorAtItsOpponent() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(aiPlayer, ManaColor.RED, 1);
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 2);
        RiskFactor riskFactor = new RiskFactor();
        harness.setHand(aiPlayer, List.of(riskFactor));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(riskFactor);
        assertThat(gameData.stack.getFirst().getTargetId()).isEqualTo(opponent.getId());
    }

    @Test
    void castsStrengthOfTheTajuruWithoutMultikicker() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent creature = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        harness.addMana(aiPlayer, ManaColor.GREEN, 4);
        StrengthOfTheTajuru strengthOfTheTajuru = new StrengthOfTheTajuru();
        harness.setHand(aiPlayer, List.of(strengthOfTheTajuru));

        RandomAiDecisionEngine engine = createEngine(harness, aiPlayer, new Random() {
            @Override
            public int nextInt(int bound) {
                return bound - 1;
            }
        });
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(strengthOfTheTajuru);
        assertThat(gameData.stack.getFirst().getXValue()).isEqualTo(1);
        assertThat(gameData.stack.getFirst().getTargetId()).isEqualTo(creature.getId());
        assertThat(gameData.stack.getFirst().getRepeatedAdditionalCosts()).isEmpty();
    }

    @Test
    void castsBlackSpellWithDroughtTax() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.addToBattlefield(opponent, new Drought());
        Permanent swamp = harness.addToBattlefieldAndReturn(aiPlayer, new Swamp());
        harness.addMana(aiPlayer, ManaColor.BLACK, 1);
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 2);
        DauthiMercenary mercenary = new DauthiMercenary();
        harness.setHand(aiPlayer, List.of(mercenary));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(mercenary);
        assertThat(gameData.playerBattlefields.get(aiPlayer.getId())).doesNotContain(swamp);
    }

    @Test
    void castsCulturalExchangeWithDistinctPlayerTargets() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(aiPlayer, ManaColor.BLUE, 2);
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 4);
        CulturalExchange culturalExchange = new CulturalExchange();
        harness.setHand(aiPlayer, List.of(culturalExchange));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(culturalExchange);
        assertThat(gameData.stack.getFirst().getTargetIds())
                .containsExactly(opponent.getId(), aiPlayer.getId());
    }

    @Test
    void limitsPerTargetLifeCostTargets() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(aiPlayer, 7);
        harness.addMana(aiPlayer, ManaColor.BLACK, 2);
        harness.addMana(aiPlayer, ManaColor.RED, 2);
        harness.addToBattlefield(harness.getPlayer1(), new GrizzlyBears());
        harness.addToBattlefield(harness.getPlayer1(), new GrizzlyBears());
        harness.addToBattlefield(harness.getPlayer1(), new GrizzlyBears());
        PhyrexianPurge purge = new PhyrexianPurge();
        harness.setHand(aiPlayer, List.of(purge));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(purge);
        assertThat(gameData.stack.getFirst().getTargetIds()).hasSize(2);
        assertThat(gameData.getLife(aiPlayer.getId())).isEqualTo(1);
    }

    @Test
    void castsStriveSpellWithOnlyTheTargetsItsManaCovers() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        harness.addMana(aiPlayer, ManaColor.GREEN, 1);
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 1);
        SetessanTactics tactics = new SetessanTactics();
        harness.setHand(aiPlayer, List.of(tactics));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(tactics);
        assertThat(gameData.stack.getFirst().getTargetId()).isNotNull();
        assertThat(gameData.stack.getFirst().getTargetIds()).isEmpty();
    }

    @Test
    void castsSchemingSymmetryWithBothPlayerTargets() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(aiPlayer, ManaColor.BLACK, 1);
        SchemingSymmetry schemingSymmetry = new SchemingSymmetry();
        harness.setHand(aiPlayer, List.of(schemingSymmetry));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(schemingSymmetry);
        assertThat(gameData.stack.getFirst().getTargetIds())
                .containsExactly(opponent.getId(), aiPlayer.getId());
    }

    @Test
    void castsColoredCreatureWithHeartlessSummoningReduction() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(aiPlayer, new HeartlessSummoning());
        harness.addMana(aiPlayer, ManaColor.BLACK, 1);
        SoldeviAdnate adnate = new SoldeviAdnate();
        harness.setHand(aiPlayer, List.of(adnate));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(adnate);
        assertThat(gameData.playerHands.get(aiPlayer.getId())).isEmpty();
        assertThat(gameData.playerManaPools.get(aiPlayer.getId()).getTotal()).isZero();
    }

    @Test
    void castsDigThroughTimeUsingDelve() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(aiPlayer, new Island());
        }
        DigThroughTime digThroughTime = new DigThroughTime();
        harness.setHand(aiPlayer, List.of(digThroughTime));
        harness.setGraveyard(aiPlayer, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(digThroughTime);
        assertThat(gameData.playerGraveyards.get(aiPlayer.getId())).hasSize(4);
        assertThat(gameData.getPlayerExiledCards(aiPlayer.getId())).hasSize(4);
    }

    @Test
    void castsPyrokinesisWithItsHandExileAlternateCost() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent target = harness.addToBattlefieldAndReturn(opponent, new GrizzlyBears());
        target.setSummoningSick(false);
        Pyrokinesis pyrokinesis = new Pyrokinesis();
        LightningBolt redCard = new LightningBolt();
        harness.setHand(aiPlayer, List.of(pyrokinesis, redCard));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(pyrokinesis);
        assertThat(gameData.getPlayerExiledCards(aiPlayer.getId())).containsExactly(redCard);
        assertThat(gameData.playerManaPools.get(aiPlayer.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void skipsTargetDependentSpellWhenSelectedTargetCannotPayFullCost() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(aiPlayer, ManaColor.WHITE, 1);
        harness.addMana(aiPlayer, ManaColor.RED, 2);
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(opponent, new GrizzlyBears());
        untappedCreature.setSummoningSick(false);
        LuminousRebuke luminousRebuke = new LuminousRebuke();
        harness.setHand(aiPlayer, List.of(luminousRebuke));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerHands.get(aiPlayer.getId())).containsExactly(luminousRebuke);
        assertThat(gameData.playerManaPools.get(aiPlayer.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void castsDominateWithTargetWithinAnnouncedX() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(aiPlayer, ManaColor.BLUE, 6);
        Permanent tooExpensive = harness.addToBattlefieldAndReturn(opponent, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(opponent, new GrizzlyBears());
        Dominate dominate = new Dominate();
        harness.setHand(aiPlayer, List.of(dominate));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(dominate);
        assertThat(gameData.stack.getFirst().getTargetId()).isEqualTo(target.getId());
        assertThat(gameData.stack.getFirst().getTargetId()).isNotEqualTo(tooExpensive.getId());
        assertThat(gameData.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    void castsXSpellWithAllRequiredDiscardCards() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(aiPlayer, ManaColor.BLACK, 3);
        AbandonHope abandonHope = new AbandonHope();
        GrizzlyBears discard = new GrizzlyBears();
        harness.setHand(aiPlayer, List.of(abandonHope, discard));
        harness.setHand(opponent, List.of(new GrizzlyBears()));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(abandonHope);
        assertThat(gameData.stack.getFirst().getXValue()).isEqualTo(1);
        assertThat(gameData.playerHands.get(aiPlayer.getId())).isEmpty();
        assertThat(gameData.playerGraveyards.get(aiPlayer.getId()))
                .containsExactly(discard);
    }

    @Test
    void castsFixedMultiCardDiscardSpellWithAllRequiredDiscardCards() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(aiPlayer, ManaColor.RED, 1);
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 1);
        CatharticReunion reunion = new CatharticReunion();
        GrizzlyBears firstDiscard = new GrizzlyBears();
        GrizzlyBears secondDiscard = new GrizzlyBears();
        GrizzlyBears remainingCard = new GrizzlyBears();
        harness.setHand(aiPlayer, List.of(reunion, firstDiscard, secondDiscard, remainingCard));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(reunion);
        assertThat(gameData.playerGraveyards.get(aiPlayer.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard);
        assertThat(gameData.playerHands.get(aiPlayer.getId())).containsExactly(remainingCard);
    }

    @Test
    void castsPyrrhicStrikeWithoutOptionalBlightForSingleMode() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(aiPlayer, ManaColor.WHITE, 3);
        Permanent blightCreature = harness.addToBattlefieldAndReturn(aiPlayer, new HillGiant());
        Permanent artifact = harness.addToBattlefieldAndReturn(opponent, new Ornithopter());
        harness.setHand(aiPlayer, List.of(new PyrrhicStrike()));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getTargetIds()).containsExactly(artifact.getId());
        assertThat(blightCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void castsTorrentOfSoulsWithoutOptionalGraveyardTarget() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(aiPlayer, ManaColor.BLACK, 2);
        harness.addMana(aiPlayer, ManaColor.RED, 3);
        TorrentOfSouls torrentOfSouls = new TorrentOfSouls();
        harness.setHand(aiPlayer, List.of(torrentOfSouls));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(torrentOfSouls);
        assertThat(gameData.stack.getFirst().getTargetId()).isNull();
        assertThat(gameData.stack.getFirst().getTargetIds()).containsExactly(opponent.getId());
    }

    @Test
    void doesNotCastVictimizeWithoutTwoCreatureCardsInGraveyard() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(aiPlayer, ManaColor.BLACK, 3);
        gameData.playerGraveyards.get(aiPlayer.getId()).add(new HolyDay());
        Victimize victimize = new Victimize();
        harness.setHand(aiPlayer, List.of(victimize));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerHands.get(aiPlayer.getId())).containsExactly(victimize);
        assertThat(gameData.playerManaPools.get(aiPlayer.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void castsDynamicUpToSpellWithNoAvailableTargets() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(aiPlayer, ManaColor.BLACK, 3);
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        MogissMarauder marauder = new MogissMarauder();
        harness.setHand(aiPlayer, List.of(marauder));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(marauder);
        assertThat(gameData.playerHands.get(aiPlayer.getId())).isEmpty();
    }

    @Test
    void excludesCanOnlyAttackAloneCreatureAndIgnoresStaleRetry() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();
        harness.setLife(opponent, 20);

        Permanent restricted = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        restricted.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(aiPlayer, new Errantry());
        aura.setAttachedTo(restricted.getId());
        Permanent unrestricted = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        unrestricted.setSummoningSick(false);
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);
            TurnStep stepAfterDeclaration = gameData.currentStep;

            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
            assertThat(gameData.currentStep).isEqualTo(stepAfterDeclaration);
        } finally {
            watcher.uninstall();
        }

        assertThat(restricted.isAttacking()).isFalse();
        assertThat(gameData.getLife(opponent.getId())).isEqualTo(18);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    void respectsBattlefieldWideAttackerLimit() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();
        harness.setLife(opponent, 2);

        harness.addToBattlefield(opponent, new DuelingGrounds());
        Permanent first = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        first.setSummoningSick(false);
        Permanent second = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        second.setSummoningSick(false);
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(List.of(first, second).stream().filter(Permanent::isAttacking).count()).isEqualTo(1);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class))
                .isNull();
    }

    @Test
    void declaresMagnetCounterCreaturesTogether() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();
        harness.setLife(opponent, 20);

        harness.addToBattlefield(aiPlayer, new MagneticWeb());
        Permanent first = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        first.setSummoningSick(false);
        second.setSummoningSick(false);
        first.setCounterCount(CounterType.MAGNET, 1);
        second.setCounterCount(CounterType.MAGNET, 1);

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        RandomAiDecisionEngine engine = createEngine(harness, aiPlayer, new Random() {
            private int decisions;

            @Override
            public boolean nextBoolean() {
                return decisions++ == 0;
            }
        });
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(first.isAttacking()).isTrue();
        assertThat(second.isAttacking()).isTrue();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class))
                .isNull();
    }

    @Test
    void doesNotDeclareOrcishConscriptsWithoutTwoOtherAttackers() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        Permanent conscripts = harness.addToBattlefieldAndReturn(aiPlayer, new OrcishConscripts());
        conscripts.setSummoningSick(false);
        Permanent ally = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        ally.setSummoningSick(false);
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(conscripts.isAttackedThisTurn()).isFalse();
        assertThat(ally.isAttackedThisTurn()).isTrue();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    void sendsRequiredAttackerToAvailablePlaneswalker() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.addToBattlefield(opponent, new IslandSanctuary());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(opponent, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        Permanent blocker = harness.addToBattlefieldAndReturn(opponent, new GrizzlyBears());
        blocker.setSummoningSick(false);
        Permanent attacker = harness.addToBattlefieldAndReturn(aiPlayer, new Ramroller());
        attacker.setSummoningSick(false);
        gameData.turnNumber = 2;

        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(opponent, true);

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.inMutationScope(() -> harness.getCombatAttackService().handleDeclareAttackersStep(gameData));

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(attacker.isAttacking()).isTrue();
        assertThat(attacker.getAttackTarget()).isEqualTo(planeswalker.getId());
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class))
                .isNull();
    }

    @Test
    void acceptsUnfulfillableMustAttackRequirementForOrcishConscripts() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        Permanent conscripts = harness.addToBattlefieldAndReturn(aiPlayer, new OrcishConscripts());
        conscripts.setSummoningSick(false);
        conscripts.setMustAttackThisTurn(true);
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(conscripts.isAttacking()).isFalse();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    void doesNotDeclareOkkWithoutGreaterPowerBlocker() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent okk = blockScenario(harness, new GrizzlyBears(), new Okk());

        declareBlockersAsRandomAi(harness);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(okk.isBlocking()).isFalse();
    }

    @Test
    void acceptsUnfulfillableMustBlockRequirementForOkk() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent attacker = harness.addToBattlefieldAndReturn(harness.getPlayer1(), new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent okk = harness.addToBattlefieldAndReturn(harness.getPlayer2(), new Okk());
        okk.setSummoningSick(false);
        okk.getMustBlockIds().add(attacker.getId());

        declareBlockersAsRandomAi(harness);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(okk.isBlocking()).isFalse();
    }

    @Test
    void doesNotDeclareOrcishConscriptsWithoutTwoOtherBlockers() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent conscripts = blockScenario(harness, new HillGiant(), new OrcishConscripts());

        declareBlockersAsRandomAi(harness);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(conscripts.isBlocking()).isFalse();
    }

    @Test
    void doesNotBlockHighPowerAttackerWithHipparionItCannotPayFor() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent hipparion = blockScenario(harness, new HillGiant(), new Hipparion());

        declareBlockersAsRandomAi(harness);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(hipparion.isBlocking()).isFalse();
    }

    @Test
    void blocksHighPowerAttackerWithHipparionWhenTheBlockCostIsPaid() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent hipparion = blockScenario(harness, new HillGiant(), new Hipparion());
        harness.addMana(harness.getPlayer2(), ManaColor.WHITE, 1);

        declareBlockersAsRandomAi(harness);

        assertThat(hipparion.isBlocking()).isTrue();
        assertThat(gameData.playerManaPools.get(harness.getPlayer2().getId()).getTotal()).isZero();
    }

    @Test
    void doesNotBlockAttackerTaxedByAnAuraItCannotPayFor() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent blocker = blockScenario(harness, new HillGiant(), new GrizzlyBears());
        enchantAttackerWithAwesomePresence(harness);

        declareBlockersAsRandomAi(harness);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void blocksAttackerTaxedByAnAuraWhenTheBlockCostIsPaid() {
        GameTestHarness harness = new GameTestHarness();
        GameData gameData = harness.getGameData();
        Permanent blocker = blockScenario(harness, new HillGiant(), new GrizzlyBears());
        enchantAttackerWithAwesomePresence(harness);
        harness.addMana(harness.getPlayer2(), ManaColor.GREEN, 3);

        declareBlockersAsRandomAi(harness);

        assertThat(blocker.isBlocking()).isTrue();
        assertThat(gameData.playerManaPools.get(harness.getPlayer2().getId()).getTotal()).isZero();
    }

    @Test
    void blocksWithHipparionForFreeBelowItsPowerThreshold() {
        GameTestHarness harness = new GameTestHarness();
        Permanent hipparion = blockScenario(harness, new GrizzlyBears(), new Hipparion());

        declareBlockersAsRandomAi(harness);

        assertThat(hipparion.isBlocking()).isTrue();
    }

    @Test
    void capsLureBlocksToAuraGrantedMaximum() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        Permanent attacker = harness.addToBattlefieldAndReturn(harness.getPlayer1(), new KjeldoranRoyalGuard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent authority = harness.addToBattlefieldAndReturn(harness.getPlayer1(), new AlphaAuthority());
        authority.setAttachedTo(attacker.getId());
        Permanent lure = harness.addToBattlefieldAndReturn(harness.getPlayer1(), new Lure());
        lure.setAttachedTo(attacker.getId());
        Permanent firstBlocker = harness.addToBattlefieldAndReturn(harness.getPlayer2(), new GrizzlyBears());
        firstBlocker.setSummoningSick(false);
        Permanent secondBlocker = harness.addToBattlefieldAndReturn(harness.getPlayer2(), new GrizzlyBears());
        secondBlocker.setSummoningSick(false);

        declareBlockersAsRandomAi(harness);

        assertThat(List.of(firstBlocker, secondBlocker)).filteredOn(Permanent::isBlocking).hasSize(1);
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class))
                .isNull();
    }

    @Test
    void reselectsSpellTargetRemovedWhileTappingMana() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(opponent, new GrizzlyBears());
        harness.addToBattlefield(aiPlayer, new StormCauldron());
        harness.addToBattlefield(aiPlayer, new Island());
        harness.addToBattlefield(aiPlayer, new Island());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(aiPlayer, new Forest());
        }
        harness.setHand(aiPlayer, List.of(new Confiscate()));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = new RandomAiDecisionEngine(
                gameData.id,
                aiPlayer,
                harness.getGameRegistry(),
                harness.getGameService(),
                harness.getGameQueryService(),
                harness.getBlockLegalityService(),
                harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(),
                harness.getCastingCostService(),
                harness.getCastingPermissionService(),
                harness.getTargetValidationService(),
                harness.getTargetLegalityService(),
                new Random() {
                    @Override
                    public int nextInt(int bound) {
                        return bound > 2 ? 2 : 0;
                    }
                },
                new FuzzTelemetry());

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getCard().getName()).isEqualTo("Confiscate");
            assertThat(gameData.stack.getFirst().getTargetId()).isEqualTo(opponentCreature.getId());
        } finally {
            watcher.uninstall();
        }
    }

    @Test
    void givesUpWhenManaPaymentCyclesTappedTargetCandidates() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        Permanent firstTarget = harness.addToBattlefieldAndReturn(opponent, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(opponent, new GrizzlyBears());
        firstTarget.tap();

        Card spell = new Card();
        spell.setName("Cycling tapped-target spell");
        spell.setType(CardType.INSTANT);
        spell.setManaCost("{3}");
        spell.target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsTappedPredicate())),
                "Target must be a tapped creature"))
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

        harness.addMana(aiPlayer, ManaColor.COLORLESS, 3);
        harness.setHand(aiPlayer, List.of(spell));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = new RandomAiDecisionEngine(
                gameData.id,
                aiPlayer,
                harness.getGameRegistry(),
                harness.getGameService(),
                harness.getGameQueryService(),
                harness.getBlockLegalityService(),
                harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(),
                harness.getCastingCostService(),
                harness.getCastingPermissionService(),
                harness.getTargetValidationService(),
                harness.getTargetLegalityService(),
                new Random() {
                    @Override
                    public int nextInt(int bound) {
                        return 0;
                    }
                },
                new FuzzTelemetry()) {
            @Override
            protected boolean tapManaForSpell(GameData data, Card card, Integer xValue,
                                              int targetingTax, int delveReduction, int costReduction,
                                              Set<UUID> excludedPermanentIds) {
                if (firstTarget.isTapped()) {
                    firstTarget.untap();
                } else {
                    firstTarget.tap();
                }
                if (secondTarget.isTapped()) {
                    secondTarget.untap();
                } else {
                    secondTarget.tap();
                }
                return false;
            }
        };

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerHands.get(aiPlayer.getId())).containsExactly(spell);
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotSubmitBattlefieldPermanentAsGraveyardTarget() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();
        GrizzlyBears graveyardCreature = new GrizzlyBears();

        harness.addToBattlefield(opponent, new GrizzlyBears());
        harness.addMana(aiPlayer, ManaColor.WHITE, 3);
        harness.setGraveyard(aiPlayer, List.of(graveyardCreature));
        harness.setHand(aiPlayer, List.of(new ReturnToTheRanks()));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createEngine(harness, aiPlayer, new Random() {
            @Override
            public boolean nextBoolean() {
                return true;
            }

            @Override
            public int nextInt(int bound) {
                return 0;
            }
        });
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        assertThat(gameData.playerHands.get(aiPlayer.getId())).isEmpty();
    }

    @Test
    void choosesAtMostOneCardOfEachTypeForPullFromTheDeep() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();
        HolyDay firstInstant = new HolyDay();
        HolyDay secondInstant = new HolyDay();
        LavaAxe sorcery = new LavaAxe();
        PullFromTheDeep spell = new PullFromTheDeep();

        harness.setGraveyard(aiPlayer, List.of(firstInstant, secondInstant, sorcery));
        harness.setHand(aiPlayer, List.of(spell));
        harness.addMana(aiPlayer, ManaColor.BLUE, 4);
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createEngine(harness, aiPlayer, new Random() {
            @Override
            public boolean nextBoolean() {
                return false;
            }

            @Override
            public int nextInt(int bound) {
                return 0;
            }
        });
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(gameData.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                    .isNotNull();

            engine.handleEvent(AiDecisionKind.INTERACTION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        StackEntry stackEntry = gameData.stack.stream()
                .filter(entry -> entry.getCard() == spell)
                .findFirst()
                .orElseThrow();
        assertThat(stackEntry.getTargetCardIds())
                .containsExactly(firstInstant.getId(), sorcery.getId());
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotCastGraveyardReturnSpellWhenGroundSealBlocksTargets() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();
        DeathsDuet spell = new DeathsDuet();

        harness.addToBattlefield(opponent, new GroundSeal());
        harness.setGraveyard(aiPlayer, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(aiPlayer, List.of(spell));
        harness.addMana(aiPlayer, ManaColor.BLACK, 3);
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.playerHands.get(aiPlayer.getId())).containsExactly(spell);
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void suppliesUntappedCreaturesForConvokeSpell() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.addToBattlefield(aiPlayer, new Island());
        harness.addToBattlefield(aiPlayer, new Island());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        firstCreature.setSummoningSick(false);
        secondCreature.setSummoningSick(false);

        Card convokeSpell = new Card();
        convokeSpell.setName("Convoke Test Creature");
        convokeSpell.setType(CardType.CREATURE);
        convokeSpell.setManaCost("{3}{U}");
        convokeSpell.setPower(4);
        convokeSpell.setToughness(4);
        convokeSpell.setKeywords(EnumSet.of(Keyword.CONVOKE));
        harness.setHand(aiPlayer, List.of(convokeSpell));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);
            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(convokeSpell);
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
    }

    @Test
    void skipsXSpellWhenItsDynamicCapIsZero() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        Permanent attacker = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(aiPlayer, new Island());
        }
        harness.setHand(aiPlayer, List.of(new WintersChill()));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerHands.get(aiPlayer.getId()))
                .extracting(Card::getName)
                .containsExactly("Winter's Chill");
        assertThat(gameData.playerBattlefields.get(aiPlayer.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Island"))
                .allMatch(permanent -> !permanent.isTapped());
    }

    @Test
    void coSelectsStirTheGraveTargetAndX() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();
        GrizzlyBears target = new GrizzlyBears();

        harness.addMana(aiPlayer, ManaColor.BLACK, 3);
        harness.setGraveyard(aiPlayer, List.of(target));
        harness.setHand(aiPlayer, List.of(new StirTheGrave()));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createEngine(harness, aiPlayer, new Random() {
            @Override
            public boolean nextBoolean() {
                return true;
            }

            @Override
            public int nextInt(int bound) {
                return 0;
            }
        });
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getTargetId()).isEqualTo(target.getId());
        assertThat(gameData.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    void passesPriorityWhenNoGraveyardCreatureManaCostIsPayable() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        harness.addToBattlefield(aiPlayer, new BackFromTheBrink());
        Permanent forest = harness.addToBattlefieldAndReturn(aiPlayer, new Forest());
        forest.tap();
        harness.setGraveyard(aiPlayer, List.of(new LlanowarElves()));
        harness.setHand(aiPlayer, List.of());
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);

        engine.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(gameData.priorityPassedBy).contains(aiPlayer.getId());
    }

    @Test
    void paysForAndExilesTheAffordableGraveyardCreature() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();
        LlanowarElves affordableCreature = new LlanowarElves();

        harness.addToBattlefield(aiPlayer, new BackFromTheBrink());
        Permanent forest = harness.addToBattlefieldAndReturn(aiPlayer, new Forest());
        harness.setGraveyard(aiPlayer, List.of(affordableCreature, new GrizzlyBears()));
        harness.setHand(aiPlayer, List.of());
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);

        engine.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gameData.interaction.isAwaitingInput()).isTrue();

        engine.handleEvent(AiDecisionKind.INTERACTION);

        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
        assertThat(gameData.playerGraveyards.get(aiPlayer.getId()))
                .extracting(Card::getId)
                .doesNotContain(affordableCreature.getId());
        assertThat(gameData.stack).hasSize(1);
    }

    @Test
    void castsSpellWithMultiPermanentSacrificeCost() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player opponent = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        harness.addToBattlefield(opponent, new HowlingMine());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(aiPlayer, new Swamp());
        }
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        harness.addToBattlefield(aiPlayer, new GrizzlyBears());
        harness.setHand(aiPlayer, List.of(new PhyrexianTribute()));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
            assertThat(gameData.stack).hasSize(1);
            assertThat(gameData.stack.getFirst().getCard().getName()).isEqualTo("Phyrexian Tribute");
            assertThat(gameData.playerGraveyards.get(aiPlayer.getId()))
                    .extracting(Card::getName)
                    .containsExactly("Grizzly Bears", "Grizzly Bears");
        } finally {
            watcher.uninstall();
        }
    }

    @Test
    void castsTorgaarWithSacrificeCostReduction() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        Permanent fodder = harness.addToBattlefieldAndReturn(aiPlayer, new TolarianScholar());
        harness.addMana(aiPlayer, ManaColor.BLACK, 2);
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 4);
        TorgaarFamineIncarnate torgaar = new TorgaarFamineIncarnate();
        harness.setHand(aiPlayer, List.of(torgaar));
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.GAME_STATE);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getCard()).isSameAs(torgaar);
        assertThat(gameData.playerBattlefields.get(aiPlayer.getId())).doesNotContain(fodder);
    }

    @Test
    void declaresAttackersForMindslaveredPlayer() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player controlled = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        Permanent berserkers = mindslavedAttackerDeclaration(harness);

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(berserkers.isAttacking()).isTrue();
        assertThat(gameData.playerBattlefields.get(aiPlayer.getId())).noneMatch(Permanent::isAttacking);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
        assertThat(controlled.getId()).isEqualTo(gameData.activePlayerId);
    }

    @Test
    void declaresBlockersAfterMindslaveredPlayerAttacks() {
        GameTestHarness harness = new GameTestHarness();
        harness.skipMulligan();
        GameData gameData = harness.getGameData();
        Player aiPlayer = harness.getPlayer2();

        Permanent berserkers = mindslavedAttackerDeclaration(harness);
        Permanent blocker = gameData.playerBattlefields.get(aiPlayer.getId()).getLast();
        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, aiPlayer);
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);
            assertThat(berserkers.isAttacking()).isTrue();
            assertThat(gameData.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class))
                    .isNotNull();

            engine.handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(gameData.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class)).isNull();
        assertThat(blocker.isBlocking()).isTrue();
    }

    /**
     * Hands the AI seat control of player1's turn through a real Mindslaver activation and stops in
     * that turn's declare-attackers step, returning the controlled player's must-attack creature.
     *
     * <p>The engine addresses the attacker decision to the controller while the interaction stays
     * owned by the controlled player, so the AI has to declare from a battlefield that is not its
     * own. The controlled player is deliberately given more permanents than the AI seat and the
     * must-attack creature is put last, so an engine reading its own battlefield can only produce
     * indices that name something else — or nothing at all.
     */
    private Permanent mindslavedAttackerDeclaration(GameTestHarness harness) {
        GameData gameData = harness.getGameData();
        Player controlled = harness.getPlayer1();
        Player aiPlayer = harness.getPlayer2();

        Set<TurnStep> mainPhaseStops = Set.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
        for (Player player : List.of(controlled, aiPlayer)) {
            gameData.playerAutoStopSteps.put(player.getId(), ConcurrentHashMap.newKeySet());
            gameData.playerAutoStopSteps.get(player.getId()).addAll(mainPhaseStops);
        }

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(aiPlayer, new Mindslaver());
        harness.addMana(aiPlayer, ManaColor.COLORLESS, 4);
        harness.activateAbility(aiPlayer, 0, null, controlled.getId());
        harness.passBothPriorities();

        // Roll into the controlled player's turn, where the control actually takes effect.
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        assertThat(gameData.activePlayerId).isEqualTo(controlled.getId());
        assertThat(gameData.mindControlledPlayerId).isEqualTo(controlled.getId());
        assertThat(gameData.mindControllerPlayerId).isEqualTo(aiPlayer.getId());

        Permanent ownBears = harness.addToBattlefieldAndReturn(aiPlayer, new GrizzlyBears());
        ownBears.setSummoningSick(false);

        for (int i = 0; i < 3; i++) {
            harness.addToBattlefieldAndReturn(controlled, new Forest()).setSummoningSick(false);
        }
        Permanent berserkers = harness.addToBattlefieldAndReturn(controlled, new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        return berserkers;
    }

    /**
     * Sets up a one-attacker combat: {@code attackerCard} attacking for {@link GameTestHarness#getPlayer1()},
     * {@code blockerCard} untapped for the AI seat. Returns the blocker so the test can assert on it.
     */
    private Permanent blockScenario(GameTestHarness harness, Card attackerCard, Card blockerCard) {
        Permanent attacker = harness.addToBattlefieldAndReturn(harness.getPlayer1(), attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(harness.getPlayer2(), blockerCard);
        blocker.setSummoningSick(false);
        return blocker;
    }

    /**
     * Attaches an Awesome Presence controlled by the attacking player to the sole attacker, so
     * every blocker declared against it costs the AI an extra {3}.
     */
    private void enchantAttackerWithAwesomePresence(GameTestHarness harness) {
        Permanent attacker = harness.getGameData().playerBattlefields.get(harness.getPlayer1().getId()).stream()
                .filter(Permanent::isAttacking)
                .findFirst()
                .orElseThrow();
        Permanent aura = harness.addToBattlefieldAndReturn(harness.getPlayer1(), new AwesomePresence());
        aura.setAttachedTo(attacker.getId());
    }

    /**
     * Opens blocker declaration and lets the Random AI declare, asserting the engine took the
     * declaration as sent. The AI answers "yes" to every optional decision, so it declares every
     * block it believes legal; the engine rejecting one is only logged before the AI falls back to
     * no blockers, so declining to block and being refused a block are indistinguishable on the
     * board and the log is the only place they differ.
     */
    private void declareBlockersAsRandomAi(GameTestHarness harness) {
        harness.forceActivePlayer(harness.getPlayer1());
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        RandomAiDecisionEngine engine = createAlwaysActivateEngine(harness, harness.getPlayer2());
        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            engine.handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }
    }

    private RandomAiDecisionEngine createAlwaysActivateEngine(
            GameTestHarness harness, Player aiPlayer) {
        return createEngine(harness, aiPlayer, new Random() {
            @Override
            public boolean nextBoolean() {
                return true;
            }
        });
    }

    private RandomAiDecisionEngine createEngine(
            GameTestHarness harness, Player aiPlayer, Random random) {
        return new RandomAiDecisionEngine(
                harness.getGameData().id,
                aiPlayer,
                harness.getGameRegistry(),
                harness.getGameService(),
                harness.getGameQueryService(),
                harness.getBlockLegalityService(),
                harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(),
                harness.getCastingCostService(),
                harness.getCastingPermissionService(),
                harness.getTargetValidationService(),
                harness.getTargetLegalityService(),
                random,
                new FuzzTelemetry());
    }
}
