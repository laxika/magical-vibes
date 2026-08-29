package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.HeadlessSimulationContext;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.cards.t.TroveOfTemptation;
import com.github.laxika.magicalvibes.cards.t.TragedyFeaster;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AbandonHope;
import com.github.laxika.magicalvibes.cards.b.BairdStewardOfArgive;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.b.Blight;
import com.github.laxika.magicalvibes.cards.b.BlindingBeam;
import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.SeveredLegion;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CrypticCommand;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.e.Errantry;
import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.cards.f.FitOfRage;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GoblinChieftain;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.Mindslaver;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.b.BogardanFirefiend;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.k.KillerBees;
import com.github.laxika.magicalvibes.cards.v.VigilForTheLost;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.n.Nekrataal;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Slagstorm;
import com.github.laxika.magicalvibes.cards.s.SmiteTheMonstrous;
import com.github.laxika.magicalvibes.cards.s.SteelSabotage;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.Unbury;
import com.github.laxika.magicalvibes.cards.v.Vivisection;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.model.CounterType;

abstract class HardAiDecisionEngineTestSupport {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
    }

    protected HardAiDecisionEngine createHardAi(Player aiPlayer) {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, aiPlayer.getId(), aiPlayer.getUsername());
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, aiPlayer, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        ai.setMctsEngine(new MCTSEngine(HeadlessSimulationContext.getSimulator(), 42L, 500));
        return ai;
    }

    protected void givePlayerIslands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent island = new Permanent(new Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(island);
        }
    }

    protected void giveAiPriority(Player aiPlayer) {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
    }

    /**
     * Replaces both players' hands and libraries with cards that cannot change the board, so the
     * rollouts measure the decision under test and nothing else. A seeded {@link MCTSEngine} is not
     * on its own enough for a reproducible verdict: {@code GameSetupService} shuffles both libraries
     * with an unseeded {@code Random} and deals random opening hands, and the rollouts draw from
     * them and cast what they draw. Use this wherever the margin between the two root actions is
     * thinner than the swing one drawn creature is worth.
     */
    protected void pinLibrariesAndHands() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, inertLibrary());
        harness.setLibrary(player2, inertLibrary());
    }

    protected static List<Card> inertLibrary() {
        return IntStream.range(0, 30).mapToObj(i -> (Card) new Forest()).toList();
    }

}
